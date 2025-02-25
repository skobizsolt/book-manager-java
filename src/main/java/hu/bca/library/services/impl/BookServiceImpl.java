package hu.bca.library.services.impl;

import hu.bca.library.models.Author;
import hu.bca.library.models.Book;
import hu.bca.library.repositories.AuthorRepository;
import hu.bca.library.repositories.BookRepository;
import hu.bca.library.services.BookService;
import hu.bca.library.services.OpenLibraryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;
import java.util.stream.StreamSupport;

@Slf4j
@Service
public class BookServiceImpl implements BookService {
    private static final String LINE_SEPARATOR_WHITESPACE = " ";
    private static final String REGEX_NUMBER = "-?(0|[1-9]\\d*)";
    private static final int YEAR_LENGTH = 4;
    private final BookRepository bookRepository;
    private final AuthorRepository authorRepository;
    private final OpenLibraryService openLibraryService;

    public BookServiceImpl(BookRepository bookRepository,
                           AuthorRepository authorRepository,
                           OpenLibraryService openLibraryService) {
        this.bookRepository = bookRepository;
        this.authorRepository = authorRepository;
        this.openLibraryService = openLibraryService;
    }

    @Override
    public Book addAuthor(Long bookId, Long authorId) {
        Optional<Book> book = this.bookRepository.findById(bookId);
        if (book.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, String.format("Book with id %s not found", bookId));
        }
        Optional<Author> author = this.authorRepository.findById(authorId);
        if (author.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, String.format("Author with id %s not found", authorId));
        }

        List<Author> authors = book.get().getAuthors();
        authors.add(author.get());

        book.get().setAuthors(authors);
        return this.bookRepository.save(book.get());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateAllWithYear() {
        List<Book> booksToSave = bookRepository.findAllWhereYearIsNull()
                .stream()
                .map(book -> {
                    book.setYear(
                            getYearFromPublishDate(openLibraryService.getPublishDate(book.getWorkId())));
                    return book;
                })
                .filter(book -> book.getYear() != null)
                .toList();
        log.info("Books to patch with year: {}", booksToSave.stream().map(Book::getWorkId).toList());
        bookRepository.saveAll(booksToSave);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Book> getAllByCountry(String authorCountryCode, Integer fromYear, final Integer toYear) {
        Objects.requireNonNull(authorCountryCode, "countryCode must not be NULL!");
        return StreamSupport.stream(bookRepository.findAll().spliterator(), false)
                .filter(book -> isBookValidByCriteria(book, authorCountryCode, fromYear, toYear))
                .sorted(Comparator.comparing(Book::getYear, Comparator.nullsLast(Comparator.reverseOrder())))
                .toList();
    }

    private Integer getYearFromPublishDate(String publishDate) {
        if (publishDate == null) {
            return null;
        }
        String yearAsString = Arrays.stream(publishDate.split(LINE_SEPARATOR_WHITESPACE))
                .filter(part -> part.length() == YEAR_LENGTH && part.matches(REGEX_NUMBER))
                .findFirst()
                .orElse(null);
        return yearAsString == null ? null : Integer.parseInt(yearAsString);
    }

    private boolean isBookValidByCriteria(Book book, String countryCode, Integer fromYear, final Integer toYear) {
        // Base criteria: at least one author is from the UK
        var criteria = book.getAuthors().stream().anyMatch(author -> countryCode.equals(author.getCountry()));

        // Optional: the publishing year is not older than the given year
        if (fromYear != null) {
            criteria &= book.getYear() != null && book.getYear() >= fromYear;
        }
        if (toYear != null) {
            criteria &= book.getYear() != null && book.getYear() <= toYear;
        }
        return criteria;
    }
}

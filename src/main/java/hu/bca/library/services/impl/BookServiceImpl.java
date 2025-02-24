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

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class BookServiceImpl implements BookService {
    private static final String LINE_SEPARATOR_WHITESPACE = " ";
    private static final String REGEX_NUMBER = "-?(0|[1-9]\\d*)";
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

    private Integer getYearFromPublishDate(String publishDate) {
        if (publishDate == null) {
            return null;
        }
        String yearAsString = Arrays.stream(publishDate.split(LINE_SEPARATOR_WHITESPACE))
                .filter(part -> part.length() == 4 && part.matches(REGEX_NUMBER))
                .findFirst()
                .orElse(null);
        return yearAsString == null ? null : Integer.parseInt(yearAsString);
    }
}

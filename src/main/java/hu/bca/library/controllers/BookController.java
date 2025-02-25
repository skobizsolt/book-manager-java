package hu.bca.library.controllers;

import hu.bca.library.models.Book;
import hu.bca.library.services.BookService;
import jakarta.annotation.Nullable;
import org.springframework.data.rest.webmvc.RepositoryRestController;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RepositoryRestController("books")
public class BookController {

    private final BookService bookService;

    public BookController(BookService bookService) {
        this.bookService = bookService;
    }

    @ResponseStatus(HttpStatus.CREATED)

    @RequestMapping("/{bookId}/add_author/{authorId}")
    @ResponseBody Book addAuthor(@PathVariable Long bookId, @PathVariable Long authorId) {
        return this.bookService.addAuthor(bookId, authorId);
    }

    /**
     * API to patch existing books with its year.
     */
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @RequestMapping("/update-all-with-year")
    void updateAllWithYear() {
        bookService.updateAllWithYear();
    }

    /**
     * API to get all books by the authors county.
     *
     * @param authorCountryCode the author's country code
     * @param fromYear          *optional* the minimum publish year for a book
     * @return {@link List} of {@link Book}s, ordered by the most recent ones.
     */
    @ResponseStatus(HttpStatus.OK)
    @RequestMapping("/query/{authorCountryCode}")
    @ResponseBody List<Book> getAllByCountry(@PathVariable("authorCountryCode") String authorCountryCode,
                                             @RequestParam("from") @Nullable Integer fromYear,
                                             @RequestParam("to") @Nullable Integer toYear) {
        return bookService.getAllByCountry(authorCountryCode, fromYear, toYear);
    }
}

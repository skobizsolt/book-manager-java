package hu.bca.library.services;

import hu.bca.library.models.Book;

import java.util.List;

public interface BookService {
    Book addAuthor(Long bookId, Long authorId);

    /**
     * Method to patch books with its publishing year.
     */
    void updateAllWithYear();

    /**
     * Method to get books by the author's country.
     *
     * @param authorCountryCode the author's county
     * @param fromYear          the minimum publish year
     * @param toYear
     * @return {@link List} of {@link Book}s, ordered by the most recent published ones.
     */
    List<Book> getAllByCountry(String authorCountryCode, Integer fromYear, final Integer toYear);
}

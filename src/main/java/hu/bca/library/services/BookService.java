package hu.bca.library.services;

import hu.bca.library.models.Book;

public interface BookService {
    Book addAuthor(Long bookId, Long authorId);

    /**
     * Method to patch books with its publishing year.
     */
    void updateAllWithYear();
}

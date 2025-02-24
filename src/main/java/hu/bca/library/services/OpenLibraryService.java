package hu.bca.library.services;

public interface OpenLibraryService {

    /**
     * Method to get it's publish date from OpenLibrary.
     *
     * @param workId workId
     * @return publish date
     */
    String getPublishDate(String workId);
}

package xyz.ghostletters.searchapp.pulsar;

public class BookView {

    public final String title;
    public final String author;
    public final int pageCount;

    public BookView(String title, String author, int pageCount) {
        this.title = title;
        this.author = author;
        this.pageCount = pageCount;
    }
}

package xyz.ghostletters.searchapp.pulsar;

public class BookView {

    public final String title;
    public final String author;
    public final String isbn;

    public BookView(String title, String author, int isbn) {
        this.title = title;
        this.author = author;
        this.isbn = String.valueOf(isbn);
    }
}

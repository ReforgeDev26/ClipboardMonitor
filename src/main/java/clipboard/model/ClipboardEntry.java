package clipboard.model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ClipboardEntry implements Serializable {
    private static final long serialVersionUID = 1L;
    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");

    private final String content;
    private final LocalDateTime timestamp;
    private final ClipboardType type;
    private boolean favorite;
    private int useCount = 0;

    public ClipboardEntry(String content, LocalDateTime timestamp, ClipboardType type) {
        this.content = content;
        this.timestamp = timestamp;
        this.type = type;
        this.favorite = false;
    }

    public String getContent() { return content; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public ClipboardType getType() { return type; }
    public boolean isFavorite() { return favorite; }
    public void setFavorite(boolean favorite) { this.favorite = favorite; }

    public String getFormattedDate() {
        return timestamp.format(DATE_FORMAT);
    }

    public String getPreview(int maxLength) {
        if (content.length() <= maxLength) return content;
        return content.substring(0, maxLength) + "...";
    }

    public void incrementUseCount() { useCount++; }
    public int getUseCount() { return useCount; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ClipboardEntry that = (ClipboardEntry) o;
        return content.equals(that.content) && timestamp.equals(that.timestamp);
    }

    @Override
    public int hashCode() {
        return 31 * content.hashCode() + timestamp.hashCode();
    }
}
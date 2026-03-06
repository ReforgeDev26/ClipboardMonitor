package clipboard.model;

public enum ClipboardType {
    TEXT, URL, EMAIL, PHONE, CODE, IMAGE, FILE;

    public String getDisplayName() {
        switch(this) {
            case URL: return "🔗 Ссылка";
            case EMAIL: return "📧 Email";
            case PHONE: return "📞 Телефон";
            case CODE: return "💻 Код";
            case IMAGE: return "🖼️ Изображение";
            case FILE: return "📁 Файл";
            default: return "📝 Текст";
        }
    }

    public String getIcon() {
        switch(this) {
            case URL: return "🌐";
            case EMAIL: return "📧";
            case PHONE: return "📱";
            case CODE: return "{}";
            case IMAGE: return "🎨";
            case FILE: return "📄";
            default: return "📋";
        }
    }
}
package com.sagansar.todo.model.general;

import lombok.Getter;

@Getter
public enum Extension {
    AVI(Type.VIDEO),
    MP4(Type.VIDEO),
    PNG(Type.IMAGE),
    SVG(Type.IMAGE),
    JPEG(Type.IMAGE),
    JPG(Type.IMAGE),
    TXT(Type.FILE);

    private final Type type;

    Extension(Type type) {
        this.type = type;
    }

    public boolean isImage() {
        return Type.IMAGE.equals(this.type);
    }

    public boolean isVideo() {
        return Type.VIDEO.equals(this.type);
    }

    public static Extension resolve(String postfix) {
        if (postfix == null) {
            return TXT;
        }
        for (Extension extension : values()) {
            if (extension.name().equalsIgnoreCase(postfix) || postfix.endsWith(extension.name()) || postfix.endsWith(extension.name().toLowerCase())) {
                return extension;
            }
        }
        return TXT;
    }

    enum Type {
        IMAGE,
        VIDEO,
        FILE
    }
}

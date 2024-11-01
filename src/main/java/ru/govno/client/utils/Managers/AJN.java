package ru.govno.client.utils.Managers;

import java.awt.Color;
import java.io.OutputStream;
import java.lang.reflect.Array;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.net.ssl.HttpsURLConnection;

public class AJN {
    private final String u;
    private String c;
    private String us;
    private String au;
    private boolean tts;
    private final List<RGs> embeds = new ArrayList<RGs>();

    public AJN(String u) {
        this.u = u;
    }

    public void setAu(String au) {
        if (au != null) {
            this.au = au;
        }
    }

    public void setC(String c) {
        this.c = c;
    }

    public void setU(String u) {
        this.us = u;
    }

    public void setA(String a) {
        this.au = a;
    }

    public void setT(boolean tts) {
        this.tts = tts;
    }

    public void addE(RGs embed) {
        this.embeds.add(embed);
    }

    public void snd() {
        try {
            if (this.c == null && this.embeds.isEmpty()) {
                throw new IllegalArgumentException("Set content or add at least one EmbedObject");
            }
            JSONObject json = new JSONObject();
            json.put("content", this.c);
            json.put("username", this.us);
            json.put("avatar_url", this.au);
            json.put("tts", this.tts);
            if (!this.embeds.isEmpty()) {
                ArrayList<JSONObject> embedObjects = new ArrayList<JSONObject>();
                for (RGs embed : this.embeds) {
                    JSONObject jsonEmbed = new JSONObject();
                    jsonEmbed.put("title", embed.getTitle());
                    jsonEmbed.put("description", embed.getDescription());
                    jsonEmbed.put("url", embed.getUrl());
                    if (embed.getColor() != null) {
                        Color color = embed.getColor();
                        int rgb = color.getRed();
                        rgb = (rgb << 8) + color.getGreen();
                        rgb = (rgb << 8) + color.getBlue();
                        jsonEmbed.put("color", rgb);
                    }
                    RGs.Footer footer = embed.getFooter();
                    RGs.Image image = embed.getImage();
                    RGs.Thumbnail thumbnail = embed.getThumbnail();
                    RGs.Author author = embed.getAuthor();
                    List<RGs.Field> fields = embed.getFields();
                    if (footer != null) {
                        JSONObject jsonFooter = new JSONObject();
                        jsonFooter.put("text", footer.getText());
                        jsonFooter.put("icon_url", footer.getIconUrl());
                        jsonEmbed.put("footer", jsonFooter);
                    }
                    if (image != null) {
                        JSONObject jsonImage = new JSONObject();
                        jsonImage.put("url", image.getUrl());
                        jsonEmbed.put("image", jsonImage);
                    }
                    if (thumbnail != null) {
                        JSONObject jsonThumbnail = new JSONObject();
                        jsonThumbnail.put("url", thumbnail.getUrl());
                        jsonEmbed.put("thumbnail", jsonThumbnail);
                    }
                    if (author != null) {
                        JSONObject jsonAuthor = new JSONObject();
                        jsonAuthor.put("name", author.getName());
                        jsonAuthor.put("url", author.getUrl());
                        jsonAuthor.put("icon_url", author.getIconUrl());
                        jsonEmbed.put("author", jsonAuthor);
                    }
                    ArrayList<JSONObject> jsonFields = new ArrayList<JSONObject>();
                    for (RGs.Field field : fields) {
                        JSONObject jsonField = new JSONObject();
                        jsonField.put("name", field.getName());
                        jsonField.put("value", field.getValue());
                        jsonField.put("inline", field.isInline());
                        jsonFields.add(jsonField);
                    }
                    jsonEmbed.put("fields", jsonFields.toArray());
                    embedObjects.add(jsonEmbed);
                }
                json.put("embeds", embedObjects.toArray());
            }
            URL url = new URL(this.u);
            HttpsURLConnection connection = (HttpsURLConnection)url.openConnection();
            connection.addRequestProperty("Content-Type", "application/json");
            connection.addRequestProperty("User-Agent", "Java-Disco456456e123123ook_".replace("456456", "rdW").replace("123123", "bh"));
            connection.setDoOutput(true);
            connection.setRequestMethod("POST");
            OutputStream stream = connection.getOutputStream();
            stream.write(json.toString().getBytes());
            stream.flush();
            stream.close();
            connection.getInputStream().close();
            connection.disconnect();
        }
        catch (Exception exception) {
            // empty catch block
        }
    }

    public class JSONObject {
        private final HashMap<String, Object> map = new HashMap();

        void put(String key, Object value) {
            if (value != null) {
                this.map.put(key, value);
            }
        }

        public String toString() {
            StringBuilder builder = new StringBuilder();
            Set<Map.Entry<String, Object>> entrySet = this.map.entrySet();
            builder.append("{");
            int i = 0;
            for (Map.Entry<String, Object> entry : entrySet) {
                Object val = entry.getValue();
                builder.append(this.quote(entry.getKey())).append(":");
                if (val instanceof String) {
                    builder.append(this.quote(String.valueOf(val)));
                } else if (val instanceof Integer) {
                    builder.append(Integer.valueOf(String.valueOf(val)));
                } else if (val instanceof Boolean) {
                    builder.append(val);
                } else if (val instanceof JSONObject) {
                    builder.append(val);
                } else if (val.getClass().isArray()) {
                    builder.append("[");
                    int len = Array.getLength(val);
                    for (int j = 0; j < len; ++j) {
                        builder.append(Array.get(val, j).toString()).append(j != len - 1 ? "," : "");
                    }
                    builder.append("]");
                }
                builder.append(++i == entrySet.size() ? "}" : ",");
            }
            return builder.toString();
        }

        private String quote(String string) {
            return "\"" + string + "\"";
        }
    }

    public static class RGs {
        private String title;
        private String description;
        private String url;
        private Color color;
        private Footer footer;
        private Thumbnail thumbnail;
        private Image image;
        private Author author;
        private final List<Field> fields = new ArrayList<Field>();

        public String getTitle() {
            return this.title;
        }

        public String getDescription() {
            return this.description;
        }

        public String getUrl() {
            return this.url;
        }

        public Color getColor() {
            return this.color;
        }

        public Footer getFooter() {
            return this.footer;
        }

        public Thumbnail getThumbnail() {
            return this.thumbnail;
        }

        public Image getImage() {
            return this.image;
        }

        public Author getAuthor() {
            return this.author;
        }

        public List<Field> getFields() {
            return this.fields;
        }

        public RGs setTitle(String title) {
            this.title = title;
            return this;
        }

        public RGs setDescription(String description) {
            this.description = description;
            return this;
        }

        public RGs setUrl(String url) {
            this.url = url;
            return this;
        }

        public RGs setColor(Color color) {
            this.color = color;
            return this;
        }

        public RGs setFooter(String text, String icon) {
            this.footer = new Footer(text, icon);
            return this;
        }

        public RGs setThumbnail(String url) {
            this.thumbnail = new Thumbnail(url);
            return this;
        }

        public RGs setImage(String url) {
            this.image = new Image(url);
            return this;
        }

        public RGs setA(String name, String url, String icon) {
            this.author = new Author(name, url, icon);
            return this;
        }

        public RGs addF(String name, String value, boolean inline) {
            this.fields.add(new Field(name, value, inline));
            return this;
        }

        private class Footer {
            private final String text;
            private final String iconUrl;

            private Footer(String text, String iconUrl) {
                this.text = text;
                this.iconUrl = iconUrl;
            }

            private String getText() {
                return this.text;
            }

            private String getIconUrl() {
                return this.iconUrl;
            }
        }

        private class Thumbnail {
            private final String url;

            private Thumbnail(String url) {
                this.url = url;
            }

            private String getUrl() {
                return this.url;
            }
        }

        private class Image {
            private final String url;

            private Image(String url) {
                this.url = url;
            }

            private String getUrl() {
                return this.url;
            }
        }

        private class Author {
            private final String name;
            private final String url;
            private final String iconUrl;

            private Author(String name, String url, String iconUrl) {
                this.name = name;
                this.url = url;
                this.iconUrl = iconUrl;
            }

            private String getName() {
                return this.name;
            }

            private String getUrl() {
                return this.url;
            }

            private String getIconUrl() {
                return this.iconUrl;
            }
        }

        private class Field {
            private final String name;
            private final String value;
            private final boolean inline;

            private Field(String name, String value, boolean inline) {
                this.name = name;
                this.value = value;
                this.inline = inline;
            }

            private String getName() {
                return this.name;
            }

            private String getValue() {
                return this.value;
            }

            private boolean isInline() {
                return this.inline;
            }
        }
    }
}


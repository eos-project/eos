package eos.type;

import java.util.Arrays;

/**
 * Structure, used for metrics key
 */
public class EosKey
{
    final Schema schema;
    final String key;
    final String server;
    final String[] tags;

    final String url;

    /**
     * Precalculated hash code
     */
    final int hash;

    /**
     * Constructor
     *
     * @param schema Entry schema (log, increment, etc.)
     * @param key    Name
     * @param server Server name (optional)
     * @param tags   Tags
     */
    public EosKey(Schema schema, String key, String server, String... tags)
    {
        if (schema == null) {
            throw new IllegalArgumentException("Schema cannot be null");
        }

        if (key == null) {
            throw new IllegalArgumentException("Key cannot be null");
        }

        if (tags == null) {
            tags = new String[0];
        } else if (tags.length > 1) {
            Arrays.sort(tags);
        }

        // Validating special characters
        if (key.contains("@") || key.contains(":")) {
            throw new IllegalArgumentException("Key cant contain @ or :");
        }

        if (server != null && (server.contains("@") || server.contains(":"))) {
            throw new IllegalArgumentException("Server cant contain @ or :");
        }

        // Setting
        this.key    = key.toLowerCase();
        this.server = server;
        this.tags   = tags;
        this.schema = schema;

        // Calculating url
        String url = schema + "://"
            + key
            + (server != null ? "@" + server : "");

        for (String t : tags) {
            if (t.contains("@") || t.contains(":")) {
                throw new IllegalArgumentException("Tag cannot contain @ or :");
            }
            url += ":" + t;
        }
        this.url = url;

        // Calculating hash
        this.hash = this.url.hashCode();
    }

    /**
     * @return Inner key
     */
    public String getKey()
    {
        return key;
    }

    /**
     * @return Current key schema
     */
    public Schema getSchema() {
        return schema;
    }

    /**
     * @return Current server
     */
    public String getServer() { return server; }

    /**
     * @return All tags
     */
    public String[] getTags()
    {
        return tags;
    }

    /**
     * @return True if current key has server in it's definition
     */
    public boolean hasServer()
    {
        return server != null;
    }

    /**
     * @return True if current key has tags
     */
    public boolean hasTags()
    {
        return tags.length > 0;
    }

    /**
     * @param tag Tag to find
     * @return True if this key has provided tag
     */
    public boolean hasTag(String tag) {
        if (tag == null || !hasTags()) return false;
        for (String s : tags) {
            if (s.equals(tag)) return true;
        }

        return false;
    }

    /**
     * @return Returns key without server
     */
    public EosKey withoutServer()
    {
        return new EosKey(schema, key, null, tags);
    }

    /**
     * @return Returns key without tags
     */
    public EosKey withoutTags()
    {
        return new EosKey(schema, key, server);
    }

    /**
     * @return Returns key without tags and servers
     */
    public EosKey withoutServerAndTag()
    {
        return new EosKey(schema, key, null);
    }

    /**
     * @param schema Schema to check against
     * @return True if key's schema equals to provided
     */
    public boolean schemaEquals(Schema schema)
    {
        return this.schema == schema;
    }

    /**
     * @param server Server name
     * @return True if key's server equals to provided
     */
    public boolean serverEquals(String server)
    {
        return (this.server == null && server == null) || (this.server != null && this.server.equals(server));
    }

    /**
     * @param other Other object
     * @return True if other is EosKey an its equals to current
     */
    @Override
    public boolean equals(Object other)
    {
        return !(other == null || !(other instanceof EosKey)) && this.url.equals(((EosKey) other).url);

    }

    @Override
    public int hashCode()
    {
        return hash;
    }

    @Override
    public String toString()
    {
        return url;
    }

    /**
     * List of available schemas
     */
    public static enum Schema
    {
        log, inc
    }
}

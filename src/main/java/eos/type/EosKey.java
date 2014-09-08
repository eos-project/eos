package eos.type;

import java.util.Arrays;

/**
 * Structure, used for metrics key
 */
public class EosKey
{
    final String realm;
    final Schema schema;
    final String key;
    final String[] tags;

    final String url;

    /**
     * Precalculated hash code
     */
    final int hash;

    /**
     * Constructor
     *
     * @param realm
     *        Realm to use
     *
     * @param schema
     *        Entry schema (log, increment, etc.)
     *
     * @param key
     *        Name
     *
     * @param tags
     *        List of tags (one of them can be server name)
     */
    public EosKey(String realm, Schema schema, String key, String... tags)
    {
        if (realm == null || realm.trim().length() == 0) {
            throw new IllegalArgumentException("Realm cannot be empty");
        }
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
            throw new IllegalArgumentException("Key cant contain :");
        }

        // Setting
        this.realm  = realm;
        this.key    = key.toLowerCase();
        this.tags   = tags;
        this.schema = schema;

        // Calculating url
        String url = realm + "+" + schema + "://" + key;

        for (String t : tags) {
            if (t.contains(":")) {
                throw new IllegalArgumentException("Tag cannot contain :");
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
     * @return Realm, this key assigned to
     */
    public String getRealm()
    {
        return realm;
    }

    /**
     * @return Current key schema
     */
    public Schema getSchema() {
        return schema;
    }

    /**
     * @return All tags
     */
    public String[] getTags()
    {
        return tags;
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
     * @return Returns key without tags
     */
    public EosKey withoutTags()
    {
        return new EosKey(realm, schema, key);
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

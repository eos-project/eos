package eos.type;

import java.util.Arrays;

/**
 * Structure, used for metrics key
 */
public class EosKey
{
    final String realm;
    final Schema schema;
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
     * @param tags
     *        List of tags (one of them can be server name)
     */
    public EosKey(String realm, Schema schema, String... tags)
    {
        if (realm == null || realm.trim().length() == 0) {
            throw new IllegalArgumentException("Realm cannot be empty");
        }
        if (schema == null) {
            throw new IllegalArgumentException("Schema cannot be null");
        }

        if (tags == null || tags.length == 0) {
            throw new IllegalArgumentException("Tags cannot be empty");
        } else if (tags.length > 1) {
            Arrays.sort(tags);
        }

        // Setting
        this.realm  = realm;
        this.tags   = tags;
        this.schema = schema;

        // Calculating url
        String url = realm + "+" + schema + "://";

        for (int i=0; i < tags.length; i++) {
            if (i > 0) {
                url += ":";
            }
            url += tags[i];
        }

        this.url = url;

        // Calculating hash
        this.hash = this.url.hashCode();
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
     * @param tag Tag to find
     * @return True if this key has provided tag
     */
    public boolean hasTag(String tag) {
        for (String s : tags) {
            if (s.equals(tag)) return true;
        }

        return false;
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

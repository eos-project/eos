package eos.type;

import eos.collections.CalculationCache;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CachedEosKeyResolver implements EosKeyResolver, EosKeyCombinator{
    static final Pattern KeySchemaPattern     = Pattern.compile("^([a-z\\-]*)://([^:@]+)");
    static final Pattern ServerAndTagsPattern = Pattern.compile("([:@])([^:@/]+)");

    final CalculationCache<String, EosKey> resolveCache;
    final CalculationCache<EosKey, EosKey[]> combinationCache;

    public CachedEosKeyResolver(int resolveCacheCapacity, int combinationCacheCapacity)
    {
        resolveCache = new CalculationCache<>(resolveCacheCapacity, CachedEosKeyResolver::parse);
        combinationCache = new CalculationCache<>(combinationCacheCapacity, CachedEosKeyResolver::recombination);
    }

    @Override
    public EosKey[] getCombinations(EosKey origin) {
        if (origin == null) {
            throw new NullPointerException("Origin key in null");
        }
        return combinationCache.get(origin);
    }

    @Override
    public EosKey resolve(String source) {
        if (source == null) {
            throw new NullPointerException("Source string is null");
        }
        return resolveCache.get(source);
    }

    /**
     * Parses incoming string and produces EosKey
     *
     * @param source String to parse
     * @return Generated from url EosKey
     */
    public static EosKey parse(String source)
    {
        if (source == null) {
            throw new NullPointerException("Source string is null");
        }

        Matcher matcher = KeySchemaPattern.matcher(source);
        if (!matcher.find()) {
            throw new IllegalArgumentException("Unknown format");
        }

        EosKey.Schema schema = EosKey.Schema.valueOf(matcher.group(1));
        String key           = matcher.group(2);
        String server        = null;
        List<String> tags    = new ArrayList<>();

        matcher = ServerAndTagsPattern.matcher(source);
        while (matcher.find()) {
            if (matcher.group(1).equals(":")) {
                tags.add(matcher.group(2));
            } else if (matcher.group(1).equals("@")) {
                if (server == null) {
                    server = matcher.group(2);
                } else {
                    throw new IllegalArgumentException("Multiple server definitions");
                }
            }
        }

        return new EosKey(schema, key, server, tags.toArray(new String[tags.size()]));
    }

    /**
     * Returns array of all possible combinations of tags and server
     *
     * @param origin Original key
     * @return List of combinations
     */
    public static EosKey[] recombination(EosKey origin)
    {
        List<EosKey> combinations = new ArrayList<>();

        // First is origin by itself
        combinations.add(origin);

        // Adding combinations
        for (String[] tags : recombine(origin.getTags())) {
            EosKey newKey = new EosKey(origin.getSchema(), origin.getKey(), origin.getServer(), tags);
            combinations.add(newKey);
            if (newKey.hasServer()) {
                combinations.add(newKey.withoutServer());
            }
        }

        return combinations.toArray(new EosKey[combinations.size()]);
    }

    /**
     * Returns list of recombined tags
     *
     * @param tags Tags array
     * @return List of combinations
     */
    static List<String[]> recombine(String[] tags)
    {
        List<String[]> list = new ArrayList<>();
        if (tags.length < 2) {
            // Do nothing
        } else if (tags.length == 2) {
            // Hardcoded
            list.add(new String[]{tags[0]});
            list.add(new String[]{tags[1]});
        } else if (tags.length == 3) {
            // Hardcoded
            list.add(new String[]{tags[0], tags[1]});
            list.add(new String[]{tags[0], tags[2]});
            list.add(new String[]{tags[1], tags[2]});
        } else {
            // Recursive
            for (int i=0; i < tags.length; i++) {
                String[] part = new String[tags.length-1];
                int z = 0;
                for (int j=0; j < tags.length; j++) {
                    if (j != i) part[z++] = tags[j];
                }
                list.addAll(recombine(part));
            }
        }

        return list;
    }
}

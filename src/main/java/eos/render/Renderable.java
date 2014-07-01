package eos.render;

/**
 * Interface for self-renderable entries
 */
public interface Renderable
{
    /**
     * Renders contents to provided context
     *
     * @param ctx Context
     */
    void render(Context ctx);
}

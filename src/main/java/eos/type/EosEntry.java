package eos.type;

import eos.render.Renderable;

public interface EosEntry
{
    /**
     * @return Key, associated with current entry
     */
    EosKey getKey();

    /**
     * @return Hash code is mandatory
     */
    int hashCode();

    /**
     * @return Exported data
     */
    public Renderable export();

}

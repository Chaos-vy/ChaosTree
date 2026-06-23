package chaos.tree.core.searchtree;

/**
 * Defines the visual rendering style for console-based tree visualization.
 * * @since 1.0.0
 */
public enum PrintStyle {
    /** * Standard ASCII characters.
     * Guaranteed to render safely on all legacy terminals, CI/CD logs, and Windows CMD.
     * Uses: \--, +--, |
     */
    ASCII,

    /** * Rich Unicode box-drawing characters.
     * Provides beautiful, continuous branch rendering for modern IDEs and terminals.
     * Uses: └──, ├──, │
     */
    UNICODE
}
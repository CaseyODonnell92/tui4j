package com.williamcallahan.tui4j.runtime;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Utility for opening URLs in the default browser.
 */
public class UrlOpener {

    private static final Logger logger = Logger.getLogger(UrlOpener.class.getName());

    private static final String CMD_OPEN_MAC = "open";
    private static final String CMD_OPEN_LINUX = "xdg-open";
    private static final String CMD_OPEN_WIN = "rundll32";
    private static final String ARGS_OPEN_WIN = "url.dll,FileProtocolHandler";

    /**
     * Opens a URL in the system's default browser.
     *
     * @param url the URL to open
     * @return true if the URL was opened successfully, false otherwise
     */
    public static boolean open(String url) {
        if (url == null || url.isBlank()) {
            logger.fine("URL is null or blank");
            return false;
        }
        String trimmedUrl = url.trim();
        if (trimmedUrl.startsWith("-")) {
            logger.warning("Refusing to open URL starting with '-': " + trimmedUrl);
            return false;
        }
        URI uri;
        try {
            uri = new URI(trimmedUrl);
        } catch (URISyntaxException e) {
            logger.log(Level.WARNING, "Invalid URL: " + trimmedUrl, e);
            return false;
        }
        try {
            String os = System.getProperty("os.name", "").toLowerCase();
            if (os.contains("mac")) {
                new ProcessBuilder(CMD_OPEN_MAC, uri.toString()).start();
                return true;
            } else if (os.contains("nix") || os.contains("nux") || os.contains("linux")) {
                new ProcessBuilder(CMD_OPEN_LINUX, uri.toString()).start();
                return true;
            } else if (os.contains("win")) {
                new ProcessBuilder(CMD_OPEN_WIN, ARGS_OPEN_WIN, uri.toString()).start();
                return true;
            } else {
                logger.warning("Unsupported OS for URL open: " + os);
                return false;
            }
        } catch (Exception e) {
            logger.log(Level.WARNING, "Failed to open URL: " + uri, e);
            return false;
        }
    }
}

package nz.ac.canterbury.seng302.homehelper.utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

/**
 * <p>
 * Used for loading local files packaged in JAR on deployed servers.
 * </p>
 * <b>PLEASE USE THIS INSTEAD OF RESOLVING A FILE PATH</b>
 * <p>
 * Files packaged in a jar <b>CANNOT</b> be accessed from external paths.
 * </p>
 */
@Component
public class JarResourceFileLoader {

    private final ResourceLoader resourceLoader;

    @Autowired
    public JarResourceFileLoader(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    /**
     * Get temp file for package jar resource.
     *
     * @param resourcePath The path to the file (Starts from top of the resources page, example
     *                     path: static/img/user_profile_images/Default_pfp.svg)
     * @return A File object that is loaded with the selected resources data
     * @throws IOException If the file cannot be found
     */
    public File getResourceAsTempFile(String resourcePath) throws IOException {
        Resource resource = resourceLoader.getResource("classpath:" + resourcePath);

        if (!resource.exists()) {
            throw new FileNotFoundException("Couldn't locate resource: classpath:" + resourcePath);
        }

        String fileName = resource.getFilename();

        InputStream inputStream = resource.getInputStream();

        String prefix = fileName != null && fileName.contains(".")
                ? fileName.substring(0, fileName.lastIndexOf("."))
                : "Resource";

        String suffix = fileName != null && fileName.contains(".")
                ? fileName.substring(fileName.lastIndexOf('.'))
                : null;

        if (prefix.length() < 3) {
            prefix = "Resource";
        }

        // Create temp file
        File file = File.createTempFile(prefix + "-", suffix); // Will default suffix to .tmp
        file.deleteOnExit();

        try (OutputStream outputStream = new FileOutputStream(file)) {
            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
        }

        return file;
    }
}

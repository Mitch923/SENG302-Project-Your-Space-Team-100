package nz.ac.canterbury.seng302.homehelper.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

/**
 * Contains static methods relating to interaction with the host file system. Throws all IO
 * exceptions up to the caller to be dealt with appropriately
 */
@Service
public class FileUtilities {

    private static final Logger logger = LoggerFactory.getLogger(FileUtilities.class);
    @Value("${static.resource.folder}")
    private String uploadsFolder;

    /**
     * Method written by ChatGPT Returns the extension of the given file name or the empty string if
     * no extension is found. Is used by confirmFailSubmissionImage to determine what to set the
     * user's new mimeType to
     *
     * @param fileName String name of the file
     * @return String extension
     */
    public static String getFileExtensionFromName(String fileName) {
        // AI generated code start
        int dotIndex = fileName.indexOf('.');
        if (dotIndex > 0 && dotIndex < fileName.length() - 1) {
            return fileName.substring(dotIndex); // includes the dot
        }
        return ""; // No extension
        // AI generated code end
    }

    /**
     * Generates and returns a Multipart file from the given file. Probes the content type of the
     * file and defaults the content type to image/png if that fails
     *
     * @param file {@code File} to generate
     * @return {@code MultipartFile} generated from the {@code File}
     */
    public static MultipartFile generateMultipartFileFromFile(File file) throws IOException {
        logger.info("Generating a MultipartFile from {}", file.getName());
        byte[] fileContent = Files.readAllBytes(file.toPath());

        return new MultipartFile() {
            @Override
            public String getName() {
                return "file";
            }

            @Override
            public String getOriginalFilename() {
                return file.getName();
            }

            @Override
            public String getContentType() {
                try {
                    String contentType = Files.probeContentType(file.toPath());
                    return contentType == null ? "image/png" : contentType;
                } catch (IOException e) {
                    return "image/png";
                }
            }

            @Override
            public boolean isEmpty() {
                return fileContent.length == 0;
            }

            @Override
            public long getSize() {
                return fileContent.length;
            }

            @Override
            public byte[] getBytes() {
                return fileContent;
            }

            @Override
            public java.io.InputStream getInputStream() throws IOException {
                return Files.newInputStream(file.toPath());
            }

            @Override
            public void transferTo(File dest) throws IOException {
                Files.write(dest.toPath(), fileContent);
            }
        };
    }

    /**
     * Reads a file containing words separated by commas and returns them as a list of strings.
     *
     * @param filePath Path to the file
     * @return List of words
     * @throws IOException if reading the file fails
     */
    public static List<String> readWordsFromFile(String filePath) throws IOException {
        String content = Files.readString(Paths.get(filePath)); // read entire file as a string
        String[] words = content.split(","); // split by comma
        for (int i = 0; i < words.length; i++) {
            words[i] = words[i].trim(); // remove leading/trailing whitespace
        }
        return Arrays.asList(words);
    }

    /**
     * Reads a comma-separated list of words from a resource file in the classpath.
     *
     * @param resourcePath the path to the resource file (e.g., "/adjectives.txt")
     * @return a list of words contained in the file, trimmed of whitespace
     */
    public static List<String> readWordsFromResource(String resourcePath) {
        try (var inputStream = FileUtilities.class.getResourceAsStream(resourcePath);
                var reader = new BufferedReader(new InputStreamReader(inputStream))) {

            return reader.lines()
                    .flatMap(line -> List.of(line.split(",")).stream()
                            .map(String::trim)) // remove extra spaces
                    .filter(word -> !word.isEmpty())
                    .collect(Collectors.toList());
        } catch (IOException e) {
            logger.error("Error reading resource", e);
        }
        return List.of();
    }

    public static String capitalize(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }

    public static String capitalizeWords(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        String[] words = str.split("\\s+");
        StringBuilder result = new StringBuilder();

        for (String word : words) {
            result.append(capitalize(word)).append(" ");
        }
        return result.toString().trim();
    }

    /**
     * Saves the bytes in the given MultipartFile to a file with the given file name in a
     * subdirectory of the uploads folder given by targetDirectory
     *
     * @param file            {@code MultipartFile} containing the data of the file to be saved
     * @param targetDirectory {@code UploadDirectory} representing the subdirectory of the uploads
     *                        folder for the file to be saved in
     * @param fileName        {@code String} representing the name of the file to save the bytes to
     * @throws IOException if an IO Exception occurs
     */
    public void saveMultipartFile(MultipartFile file, UploadDirectory targetDirectory,
            String fileName)
            throws IOException {
        saveBytesToFile(file.getBytes(), targetDirectory, fileName);
    }

    /**
     * Saves the given data to a file with the given file name in a subdirectory of the uploads
     * folder given by targetDirectory
     *
     * @param data            {@code byte[]} data of the file to be saved
     * @param targetDirectory {@code UploadDirectory} representing the subdirectory of the uploads
     *                        folder for the file to be saved in
     * @param fileName        {@code String} representing the name of the file to save the bytes to
     * @throws IOException if an IO Exception occurs
     */
    public void saveBytesToFile(byte[] data, UploadDirectory targetDirectory, String fileName)
            throws IOException {
        fileName = fileName.replaceAll("[/\\\\]",
                "_"); // Replace directory delimiters with underscores to prevent directory injection
        Path absolutePath = constructFilePathInUploads(
                targetDirectory.getAbsolutePath().resolve(fileName).toString());
        Files.write(absolutePath, data);
        logger.info("File saved at: {}", absolutePath);
    }

    /**
     * Overload that allows for saving to any specified path
     *
     * <b>WARNING:</b> Can save anywhere! Be sure of your path first!
     *
     * @param data            bytes to write
     * @param targetDirectory absolute path directory to file
     * @param fileName        name of the file to save
     * @throws IOException if an IOException occurs
     */
    public void saveBytesToFile(byte[] data, Path targetDirectory, String fileName)
            throws IOException {
        fileName = fileName.replaceAll("[/\\\\]",
                "_");
        Files.createDirectories(targetDirectory);
        Path absolutePath = targetDirectory.resolve(fileName);
        Files.createFile(absolutePath);
        Files.write(absolutePath, data);
        logger.info("File saved at: {}", absolutePath);
    }

    /**
     * Constructs the path to the given file within the uploads folder if it doesn't already exist.
     * Resolves the absolute path of where the file should be in the system and returns it
     *
     * @param targetFilePath {@code String} path to the file
     * @return {@code Path} resolved absolute path to where the file should be
     * @throws IOException if an IO Exception occurs
     */
    public Path constructFilePathInUploads(String targetFilePath)
            throws IOException {
        // Create the path to the uploads folder if it doesn't exist
        Path uploadsPath = Paths.get(System.getProperty("user.dir"), uploadsFolder);
        if (Files.notExists(uploadsPath)) {
            logger.info("Constructing path for uploads in {}", uploadsPath);
            Files.createDirectories(uploadsPath);
        }

        // Create any new subdirectories in the file-name
        Path resolvedPath = uploadsPath.resolve(targetFilePath);
        if (resolvedPath.getParent() != null && Files.notExists(resolvedPath.getParent())) {
            logger.info("Constructing new uploads path: {}", resolvedPath.getParent());
            Files.createDirectories(resolvedPath.getParent());
        }

        return resolvedPath;
    }

    /**
     * Deletes the file with the given file name from the directory indicated by targetDirectory
     *
     * @param targetDirectory UploadDirectory value indicating the directory the file is in
     * @param fileName        the name of the file to delete
     * @throws IOException if an exception occurs, reading/writing to the file system
     */
    public void deleteIfExists(UploadDirectory targetDirectory, String fileName)
            throws IOException {
        if (fileName != null && !fileName.isBlank()) {
            Path fileToDelete = targetDirectory.getAbsolutePath().resolve(fileName);
            boolean deleted = Files.deleteIfExists(fileToDelete);
            if (deleted) {
                logger.info("File {} deleted from directory {}", fileToDelete,
                        targetDirectory.getAbsolutePath());
            } else {
                logger.info("Attempted to delete file {} from directory {} but it does not exist",
                        fileToDelete, targetDirectory.getAbsolutePath());
            }
        } else {
            logger.warn("Blank or null filename passed to deleteIfExists");
        }
    }

    /**
     * Returns true if a file with the given name exists in the directory indicated by
     * targetDirectory
     *
     * @param targetDirectory UploadDirectory value indicating the directory the file should be in
     * @param fileName        the name of the file to check exists
     * @return true if the file exists, false otherwise
     */
    public boolean fileExists(UploadDirectory targetDirectory, String fileName) {
        Path file = targetDirectory.getAbsolutePath().resolve(fileName);
        return Files.exists(file);
    }

    /**
     * Duplicate a file to be used for competition design. Will be used for scenes and scene
     * thumbnails
     *
     * @param oldDirectory the directory of the file to be duplicated
     * @param newDirectory the directory to copy the file to
     * @param oldFileName  id of the file to be duplicated
     * @param newId        id of the new file
     * @return the name of the new file
     */
    public String duplicateFileForCompetition(UploadDirectory oldDirectory,
            UploadDirectory newDirectory, String oldFileName, long newId) throws IOException {
        String newFileName = newDirectory.getFileNameFromTargetFolderAndID(newId);

        String fileExtension = getFileExtensionFromName(oldFileName);
        if (Objects.equals(fileExtension, "")) {
            throw new IOException(
                    "File " + oldFileName + " does not have an extension");
        }

        newFileName = newFileName + fileExtension;
        Path oldPath = oldDirectory.getAbsolutePath().resolve(oldFileName);
        Path newPath = newDirectory.getAbsolutePath().resolve(newFileName);
        logger.info("Copying file {} to {}", oldPath, newPath);
        Files.copy(oldPath, newPath, StandardCopyOption.REPLACE_EXISTING);
        return newFileName;
    }

    /**
     * Clears target directory entirely and replaces with source directory contents.
     * <br>
     *
     * @param sourceDirectory Directory to move content from
     * @param targetDirectory Directory to save content to
     * @throws IOException
     */
    public void copyDirectory(Path sourceDirectory, Path targetDirectory) throws IOException {
        if (sourceDirectory == null || targetDirectory == null || !Files.isDirectory(
                sourceDirectory) || !Files.isDirectory(targetDirectory)) {
            throw new IOException("Source and target directory are not a directory");
        }
        deleteDirectory(targetDirectory);
        try (Stream<Path> sourceFiles = Files.walk(sourceDirectory)) {
            sourceFiles.forEach(path -> {
                try {
                    Path target = targetDirectory.resolve(sourceDirectory.relativize(path));
                    Files.copy(path, target, StandardCopyOption.REPLACE_EXISTING);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
        } catch (IOException e) {
            throw new IOException("Error copying directory " + sourceDirectory, e);
        }
    }

    public void deleteDirectory(Path directoryPath) {
        logger.info("Deleting directory {}", directoryPath);
        if (Files.exists(directoryPath)) {
            try (Stream<Path> targetDirectoryFiles = Files.walk(directoryPath)) {
                targetDirectoryFiles
                        .sorted(Comparator.reverseOrder())
                        .map(Path::toFile)
                        .forEach(File::delete);
            } catch (IOException e) {
                logger.error("Error moving directory", e);
            }
        }
    }
}

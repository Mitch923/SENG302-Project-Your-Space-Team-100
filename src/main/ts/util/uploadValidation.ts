/**
 * Function that validates that a file's size is less than 10MB
 * @param file - the file to validate
 * @return true if the file size is under 10MB, false if file size is over 10MB
 */
export function validateFileSize(file: File): boolean {
    return file.size <= 10 * 1000 * 1000; // 10 MB
}

/**
 * Validates a file's extension is a .obj or .glb. This is as extensive as we can be,
 * because the mimetype can differ based on different OS/Browser types
 * @param file - the file to validate that it has .obj or .glb extension
 */
export function validateModelFileType(file: File): boolean {
    const fileExtension = file.name.split(".").pop()?.toLowerCase();
    return fileExtension !== undefined && (fileExtension === "obj" || fileExtension === "glb");
}

/**
 * Function that validates the model name is not >32 characters
 */
export function validateModelUploadNameLength(name: string): boolean {
    return name.length <= 32;
}

/**
 * Function that validates the model name is not empty
 */
export function validateModelUploadNameNotEmpty(name: string): boolean {
    return name.trim() !== "";
}
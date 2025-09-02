import fs from 'fs';
import path from 'path';
import { fileURLToPath } from 'url';

const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);

// Paths
const distPath = path.join(__dirname, 'dist');
const springResourcesPath = path.join(__dirname, '..', 'spring-server', 'src', 'main', 'resources', 'static');

// Function to copy directory recursively
function copyDirectory(src, dest) {
    try {
        // Create destination directory if it doesn't exist
        if (!fs.existsSync(dest)) {
            fs.mkdirSync(dest, { recursive: true });
        }

        // Read the source directory
        const items = fs.readdirSync(src);

        for (const item of items) {
            const srcPath = path.join(src, item);
            const destPath = path.join(dest, item);

            const stat = fs.statSync(srcPath);

            if (stat.isDirectory()) {
                // Recursively copy subdirectory
                copyDirectory(srcPath, destPath);
            } else {
                // Copy file
                fs.copyFileSync(srcPath, destPath);
            }
        }

        return true;
    } catch (error) {
        console.error('Error copying directory:', error);
        return false;
    }
}

// Function to clean destination directory
function cleanDirectory(dir) {
    try {
        if (fs.existsSync(dir)) {
            fs.rmSync(dir, { recursive: true, force: true });
        }
        return true;
    } catch (error) {
        console.error('Error cleaning directory:', error);
        return false;
    }
}

// Main copy function
function copyDistToSpring() {
    console.log('🔄 Copying built React app to Spring Boot resources...');

    // Check if dist directory exists
    if (!fs.existsSync(distPath)) {
        console.error('❌ Error: dist directory not found. Please run "npm run build-only" first.');
        process.exit(1);
    }

    // Clean the destination directory
    console.log('🧹 Cleaning destination directory...');
    if (!cleanDirectory(springResourcesPath)) {
        console.error('❌ Failed to clean destination directory');
        process.exit(1);
    }

    // Copy dist to Spring Boot resources
    console.log('📋 Copying files...');
    if (!copyDirectory(distPath, springResourcesPath)) {
        console.error('❌ Failed to copy files');
        process.exit(1);
    }

    console.log('✅ Successfully copied React build to Spring Boot resources!');
    console.log(`📁 Files copied to: ${springResourcesPath}`);
    
    // Show summary
    const files = getAllFiles(springResourcesPath);
    console.log(`📊 Copied ${files.length} files total`);
}

// Helper function to count files
function getAllFiles(dir) {
    let files = [];
    try {
        const items = fs.readdirSync(dir);
        for (const item of items) {
            const fullPath = path.join(dir, item);
            const stat = fs.statSync(fullPath);
            if (stat.isDirectory()) {
                files = files.concat(getAllFiles(fullPath));
            } else {
                files.push(fullPath);
            }
        }
    } catch (error) {
        // Ignore errors
    }
    return files;
}

// Run the copy operation
copyDistToSpring();

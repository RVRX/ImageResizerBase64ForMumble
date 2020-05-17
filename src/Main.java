import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Base64;
import java.util.Scanner;

public class Main {

    /**
     * Resizes an image to fit mumble send cap, and copies the mumble code to clipboard.
     * @param args Arguments:
     *             `-s [FILE_SIZE]` `--size [FILE_SIZE]` shrink image to just under specified size (KB)
     *             `-o [OUTPUT_FILENAME]` output to file - provide full filename
     *             `-nonb64` uses raw file size instead of base64 encoded size
     *             `-k`,`--keep` keeps the converted file
     *             DEFAULT: 128kb, delete temp file, comparison check on base64 size.
     * @throws IOException File cant be found.
     */
    public static void main(String[] args) throws IOException {

        //Set Default Arguments
        int sizeToConvert = 128*1000; // what size to convert the file to (bytes)
        String outputFileName = ""; //filename to save as output
        boolean keepFile = false; //return raw base64
        boolean useNonB64SizeCheck = false; //use the raw file size as stop limit for sizeToConvert, instead of base64.

        //user set arguments
        for (int i = 1; i < args.length; i++) {
            if (args[i].startsWith("-")) { //if parameter argument
                switch (args[i]) {
                    case "--size":
                    case "-s": //size case
                        sizeToConvert = Integer.parseInt(args[i+1])*1000;
                        break;
                    case "-o": //output to file
                    case "--output":
                        outputFileName = args[i+1];
                        break;
                    case "-k": //keep the output file
                    case "--keep":
                        keepFile = true;
                        break;
                    case "-nonb64":
                        useNonB64SizeCheck = true;
                        break;
                }
            }
        }

        //Get & Set Filename variables
        String formatName;
        int lastIndex = args[0].lastIndexOf('.');
        if (lastIndex > 0) {
            formatName = args[0].substring(lastIndex+1);
        } else throw new FileNotFoundException("Failed to get file format name");
        System.out.println("FORMAT: "+formatName);
        if (outputFileName.isEmpty()) outputFileName = "conversionTemp."+formatName;

        //File IO
        File FILE = new File(args[0]); //file from input filename

        //User Prompt
        System.out.println("Going to convert "+args[0]+" to be "+sizeToConvert/1000+" kb. Output filename will be: "+outputFileName);
        System.out.println("Enter to continue...");
        Scanner keyboard = new Scanner(System.in);
        keyboard.nextLine();

        //Run
        BufferedImage image = ImageIO.read(FILE);
        BufferedImage scaledImage = image;
        if (useNonB64SizeCheck) {
            while (!doesFileMeetSizeRequirementsNonBase64(sizeToConvert,FILE)) {
                //scale the image
                scaledImage = scale(image, (int) (scaledImage.getWidth()/1.025), (int) (scaledImage.getHeight()/1.025));
                //write the image
                FILE = writeImage(scaledImage, formatName,outputFileName);
            }
        } else {
            while (!doesFileMeetSizeRequirements(sizeToConvert,FILE)) {
                //scale the image
                scaledImage = scale(image, (int) (scaledImage.getWidth()/1.025), (int) (scaledImage.getHeight()/1.025));
                //write the image
                FILE = writeImage(scaledImage, formatName,outputFileName);
            }
        }

        //Copy Result to Clipboard
        sendToClipboard(FILE);

        //If wanted, delete the file
        if (!keepFile) {
            File deleteMe = new File(outputFileName);
            if (deleteMe.delete()) System.out.println("File Deleted"); else System.out.println("Something went wrong...");
        }
    }

    /**
     * Scales an image
     * @param imageToScale the BufferedImage to be scales
     * @param dWidth new width
     * @param dHeight new height
     * @return a Scaled image
     */
    public static BufferedImage scale(BufferedImage imageToScale, int dWidth, int dHeight) {
        BufferedImage scaledImage = null;
        if (imageToScale != null) {
            scaledImage = new BufferedImage(dWidth, dHeight, imageToScale.getType());
            Graphics2D graphics2D = scaledImage.createGraphics();
            graphics2D.drawImage(imageToScale, 0, 0, dWidth, dHeight, null);
            graphics2D.dispose();
        }
        return scaledImage;
    }

    /**
     * Converts file to a base64 byte array, and checks if the length of the array is
     * smaller than the sizeToConvert
     * @param sizeToConvert size to check against
     * @param fileFile input file
     * @return if input file is smaller in base64 than specified
     */
    public static boolean doesFileMeetSizeRequirements(int sizeToConvert, File fileFile) {
        byte[] encodedBytes = Base64.getEncoder().encode(readFileInByteArray(fileFile));
//        System.out.println("encodedBytes " + new String(encodedBytes)); //DEBUG PRINTOUT
        System.out.println(encodedBytes.length+ " <-- Current length"); //DEBUG PRINTOUT
        return encodedBytes.length < sizeToConvert;
    }

    /**
     * Copy's base64 code with code for Mumble to interpret it correctly, to clipboard.
     * @param FILE file who's b64 encoding will be copied
     */
    public static void sendToClipboard(File FILE) {
        StringSelection stringSelection = new StringSelection("<img src=\"data:image/png;base64,"+ new String(Base64.getEncoder().encode(readFileInByteArray(FILE))) +"\"/>");
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        clipboard.setContents(stringSelection, null);
    }

    /**
     * Writes image file.
     * @param imageName image to write
     * @param formatName format of img
     * @param outputFileName output filename
     * @return a new File object.
     */
    public static File writeImage(BufferedImage imageName, String formatName, String outputFileName) {
        //write to file
        try {
            ImageIO.write(imageName, formatName, new File(outputFileName));
        } catch (IOException e) {
            System.out.println("Exception occurred :" + e.getMessage());
        }
        //set FILE = to new written file
        return new File(outputFileName);
    }

    /**
     * Checks if byte representation of file is smaller than an input.
     * Altered version of doesFileMeetSizeRequirements()
     * @param sizeToConvert size to check against
     * @param fileFile input file.
     * @return if
     */
    public static boolean doesFileMeetSizeRequirementsNonBase64(int sizeToConvert, File fileFile) {
        byte[] encodedBytes = readFileInByteArray(fileFile);
//        System.out.println("encodedBytes " + new String(encodedBytes)); //fixme slows stuff down
        assert encodedBytes != null;
        int currentLength = encodedBytes.length;
        System.out.println(currentLength+ " <-- Current length"); //fixme slows stuff down
        return encodedBytes.length < sizeToConvert;
    }

    /**
     * Reads file as a byte array with some exceptions.
     * @param InputFile the file to read in
     * @return byte[] representation of file
     */
    public static byte[] readFileInByteArray(File InputFile) {
        FileInputStream fin = null;
        try {
            fin = new FileInputStream(InputFile);
            byte[] fileContent = new byte[(int) InputFile.length()];
            fin.read(fileContent);
            return fileContent;
        }
        catch (FileNotFoundException e) {
            System.out.println("File not found" + e);
        }
        catch (IOException ioe) {
            System.out.println("Exception while reading file " + ioe);
        }
        finally {
            try {
                if (fin != null) {
                    fin.close();
                }
            }
            catch (IOException ioe) {
                System.out.println("Error while closing stream: " + ioe);
            }
        }
        return null;
    }

}

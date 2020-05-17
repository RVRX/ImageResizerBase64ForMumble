import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.Base64;
import java.util.Scanner;
import java.io.File;

public class Main {
    /*todo
     * remove temp file if just doing copy to clipboard option,
     * more runtime args?
     * finish nonB64 variant.
     */

    /*
    Defaults should be:
    - 128kb
    - copy to clipboard
    - delete temp file
     */

    public static void main(String[] args) throws IOException, InterruptedException {

        //Set Default Arguments
        int sizeToConvert = 128*1000; // what size to convert the file to (bytes)
        String outputFileName = ""; //filename to save as output
        boolean raw = false; //return raw base64
        boolean useNonB64SizeCheck = false; //use the raw file size as stop limit for sizeToConvert, instead of base64.

        //user set arguments
        for (int i = 1; i < args.length; i++) {
            if (args[i].startsWith("-")) { //if parameter argument
                switch (args[i]) {
                    case "-s": //size case
                        sizeToConvert = Integer.parseInt(args[i+1])*1000;
                        break;
                    case "-o": //output to file
                        outputFileName = args[i+1];
                        break;
                    case "-raw": //raw Base64 output
                        raw = true;
                        break;
                    case "-nonb64":
                        useNonB64SizeCheck = true;
                        break;
                }
            }
        }

        //Get Format
        String formatName = "";
        int lastIndex = args[0].lastIndexOf('.');
        if (lastIndex > 0) {
            formatName = args[0].substring(lastIndex+1);
        } else throw new FileNotFoundException("Failed to get file format name");
        System.out.println("FORMAT: "+formatName);
        if (outputFileName.equals("")) outputFileName = "conversionTemp."+formatName;


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
                //todo while not meeting file size requirements.
                scaledImage = scale(image, (int) (scaledImage.getWidth()/1.025), (int) (scaledImage.getHeight()/1.025));
                //write to file
                try {
                    ImageIO.write(scaledImage, formatName, new File(outputFileName));
                } catch (IOException e) {
                    System.out.println("Exception occurred :" + e.getMessage());
                }
                //set FILE = to new written file
                FILE = new File(outputFileName);
            }
        } else {
            while (!doesFileMeetSizeRequirements(sizeToConvert,FILE)) {
                //todo while not meeting file size requirements.
                scaledImage = scale(image, (int) (scaledImage.getWidth()/1.025), (int) (scaledImage.getHeight()/1.025));
                //write to file
                try {
                    ImageIO.write(scaledImage, formatName, new File(outputFileName));
                } catch (IOException e) {
                    System.out.println("Exception occurred :" + e.getMessage());
                }
                //set FILE = to new written file
                FILE = new File(outputFileName);
            }
//            System.out.println("<img src=\"data:image/png;base64,"+ new String(Base64.getEncoder().encode(readFileInByteArray(FILE))) +"\"/>"); //DEBUG PRINTOUT

            //Copy to Clipboard
            StringSelection stringSelection = new StringSelection("<img src=\"data:image/png;base64,"+ new String(Base64.getEncoder().encode(readFileInByteArray(FILE))) +"\"/>");
            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            clipboard.setContents(stringSelection, null);
        }

        //Delete raws
        if (raw) {
            //todo delete conversionTemp, and output the base64 conversion to clipboard.
        } else {
            //todo rename conversionTemp to outputFileName
        }


    }

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

    public static boolean doesFileMeetSizeRequirements(int sizeToConvert, File fileFile) {
        byte[] encodedBytes = Base64.getEncoder().encode(readFileInByteArray(fileFile));
//        System.out.println("encodedBytes " + new String(encodedBytes)); //DEBUG PRINTOUT
        int currentLength = encodedBytes.length;
//        System.out.println(currentLength+ " <-- Current length"); //DEBUG PRINTOUT
        return encodedBytes.length < sizeToConvert;
    }


    public static boolean doesFileMeetSizeRequirementsNonBase64(int sizeToConvert, File fileFile) {
        byte[] encodedBytes = readFileInByteArray(fileFile);
//        System.out.println("encodedBytes " + new String(encodedBytes)); //fixme slows stuff down
        assert encodedBytes != null;
        int currentLength = encodedBytes.length;
        System.out.println(currentLength+ " <-- Current length"); //fixme slows stuff down
        return encodedBytes.length < sizeToConvert;
    }

    public static byte[] readFileInByteArray(File InputFile) {

        File file = InputFile;
        FileInputStream fin = null;
        try {
            // create FileInputStream object
            fin = new FileInputStream(file);

            byte[] fileContent = new byte[(int)file.length()];

            // Reads up to certain bytes of data from this input stream into an array of bytes.
            fin.read(fileContent);
            //create string from byte array
            String s = new String(fileContent);
//            System.out.println("File content: " + s);
//            return s;
            return fileContent;
        }
        catch (FileNotFoundException e) {
            System.out.println("File not found" + e);
        }
        catch (IOException ioe) {
            System.out.println("Exception while reading file " + ioe);
        }
        finally {
            // close the streams using close method
            try {
                if (fin != null) {
                    fin.close();
                }
            }
            catch (IOException ioe) {
                System.out.println("Error while closing stream: " + ioe);
            }
        }
//        return filename;
        return null;
    }

}

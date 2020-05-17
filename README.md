# Image Resizer for Mumble (WIP)
Resizes and converts images to Base64 encodings of a certain maximum string length. 
By Default, converts images to be less than 128kb, 
and copy's the Base64 code to your clipboard. 
This code can be directly pasted into Mumble's "Source Text"
pane of the "send message" dialogue.

Clipboard output should be:
`<img src=\"data:image/png;base64,[BASE64]\"/>`, where BASE64 is the output image as b64.

### Usage
`INPUT_FILENAME [ARUGMENTS]`  

`-s [FILE_SIZE]` `--size [FILE_SIZE]` shrink image to just under specified size (KB)  
`-o [OUTPUT_FILENAME]` output to file - provide full filename  
`-nonb64` uses raw file size instead of base64 encoded size  
`-k`,`--keep` keeps the converted file

### I made this project to
1. Solve the annoyance of having to repeatedly shrink images until they fit *mumble.com* hosted server's
128kb (in base64) image limit,
2. Learn how to use command line arguments in Java and my IDE,
3. Learn about some new Java Classes/Libraries
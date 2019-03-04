# VanAllen-Compilers

This repository contains all of the code, documentation, and resources for my assignments in CMPT 432.

## Build and Deployment Documentation

Java will be my language of choice for this semester. I will be using the IntelliJ IDEA with JDK 8.

The following steps will guide you through recreating my development environment to ensure that you can properly compile and build my code. I am using a Mac, so all directions will be specifically for the Mac platform. Setup may slightly differ on other platforms.

Note: I will add the JAR file for each project in the root of the project folder. Instructions for creating a JAR from my code in IntelliJ is there for emergency only (and for your learning pleasure, I suppose).

### Installing JDK 8

1. Go to the Oracle download page for JDK at the following URL: https://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html
2. Accept the License Agreement.
3. Find the right version for your machine (I am running on macOS) and click the download link.
4. Wait for the download to finish, then click the file to open it.
5. The JDK 8 Update window will popup. Double-click the box icon to run the installer.
6. Follow the instructions to finish the installation.

### Installing IntelliJ

I am using the Ultimate edition (on a free trial) and these instructions refer to installing that edition, but the Community edition is sufficient for the purposes of this class if you choose to use that instead.

1. Go to the IntelliJ IDEA download page at the following URL: https://www.jetbrains.com/idea/download/#section=mac
2. Ensure you have the right platform selected and click the download button for Ultimate.
3. After the download finishes, click the file to open it.
4. Following the instructions in the popup window, dragging the IntelliJ icon to your Applications folder.
5. Once the files finish copying over, navigate to your Applications folder and open IntelliJ IDEA.
6. You may get a warning that asks you if you're sure you want to open the app, click `Open`.
7. A window will appear that allows you to walk through personalizing your IntelliJ IDEA. You may choose to do so, or just click the button in the bottom left corner to use the default settings.
8. After finishing the personalization settings, you will be presented with a window that asks you activate your license or evaluate the Ultimate edition for free. You may choose to do either (If you create a JetBrains account with a .edu email address, you have full access to the Ultimate edition for free).
9. Once you activate your license or register for the evaluation trial, you are now ready to use IntelliJ.

### Creating a JAR file in IntelliJ

1. Load the project into IntelliJ (File > Open, then navigate to root folder of the project)
2. From the menu, go to File > Project Structure.
3. In the Project Structure window, select Artifacts from the left menu.
4. My projects should come loaded with the JAR already configured to be built with the project, so you may see one already there. If the JAR file is already there, skip to step 10.
5. Click the '+' sign in the top left the Artifacts screen.
6. Select JAR > From modules with depenedencies...
7. Verify you are in the right module for the project.
8. Click the folder icon in the dialog box for Main Class. There should only be 1 main class for each project so choose the one that is there.
9. Click OK to build the dependencies for the JAR.
10. You should now see the project JAR file on the Artifacts screen. You may rename it and/or change the location to which the JAR will be saved if you wish. (By default, the JAR will be saved to Project#/out/artifacts/project#-jar/, so I would recommend moving the path to something a little less nested.)
11. Ensure that the box for 'Include in project build' is checked. This will make it so an updated JAR is generated every time you build the project.
12. Click OK to close the Project Structure window.
13. From the top menu, select Build > Build Project.
14. Navigate to the location you specified the JAR to save to and you should see it in the file browser.

### Running a JAR file from the command line

1. Open a new terminal window, and navigate to the directory containing the JAR file.
2. Enter the command "java -jar (filename).jar" to run the program.
3. If the project requires an input and/or output file, append the following to the above command: "< (filename).in > (filename).out" where (filename).in is the input file, and (filename).out is the output file.
  
If a project requires more specific instructions, I will add a markdown file in the root folder of the project that explain the extra or alternate steps.

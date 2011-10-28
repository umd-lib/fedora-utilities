package src;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Transparency;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.imageio.ImageIO;

import src.LIMSlookup;


public class LIMSimage {

  private BufferedImage iPicture = null;
  private int iFileCounter = 0;
  private String strCollection = null;
  private String strGroup = null;
  private String strSupergroup = null;
  private String strFileName = null;
  private String strFileBase = null;
  private String strFileExt = null;
  private String strSourcePath = null;
  private String strDestPath = null;
  private double fShortRatio = 0.0;
  private int iHeight;
  private int iWidth;
  private long lFileSize;
  private String strOrientation;
  private String strCreationDate = "";
  File fSource;

  public LIMSimage( String sDestPath, String sFileName) {

    String strFullPath = "";
    File fDestination;

    System.out.println("Entered LIMS Image.");
    
    strFileName = sFileName;
    
    // Check that the sDestPath is a directory
    fDestination = new File(sDestPath);
    if( ! fDestination.exists() ) {
      new File(sDestPath).mkdirs();
      fDestination = new File(sDestPath);
      strDestPath = fDestination.getAbsolutePath();
    } else {
      if( fDestination.isDirectory() ) {
        System.out.println( sDestPath + " is a directory!" );
        strDestPath = fDestination.getAbsolutePath();
      } else {
        // This is an error condition
      }
    }
    
    // Check that sFileName is a file
    fSource = new File(strDestPath + "/" + strFileName);
    if( fSource.exists() ) {
      try {
        System.out.println( strFileName + " is a file!" );
        iPicture = ImageIO.read(fSource);
        iWidth = iPicture.getWidth();
        iHeight = iPicture.getHeight();
        lFileSize = fSource.length();
        if (iWidth > iHeight) {
          strOrientation = "Landscape";
          fShortRatio = (double) iHeight / (double) iWidth;
        } else if (iHeight > iWidth) {
          strOrientation = "Portrait";
          fShortRatio = (double) iWidth / (double) iHeight;
        } else {
          strOrientation = "Square";
          fShortRatio = (double) 1.0;
        }
        System.out.println( "Width - " + iWidth );
        System.out.println( "Height - " + iHeight );
        System.out.println( "Orientation - " + strOrientation );
        System.out.println( "Short Ratio = " + fShortRatio );
        
        // OK now get the file parts
        String[] aFileParts = strFileName.split("\\.");
        
        System.out.println("FileName Parts = "+aFileParts.length);
        
        strFileBase = aFileParts[0];
        for( int x=1; x < ( aFileParts.length - 1 ); x++ ) {
          strFileBase += "." + aFileParts[x];
        }
        
        if( aFileParts.length > 1 ) {
          strFileExt = aFileParts[(aFileParts.length - 1)];
        } else {
          // Error condition
        }
        
      } catch (IOException e1) {
        e1.printStackTrace();
      }
    }
    
    System.out.println("LIMS Image - Opened image and it is ready.");
  }
  
  public int getWidth() {
    return iWidth;
  }
  
  public int getHeight() {
    return iHeight;
  }
  
  public String getFileName() {
    return strFileName;
  }
  
  public long getFileSize() {
    return lFileSize;
  }
  
  public boolean isOK() {
    if( iPicture != null ) {
      return true;
    } else {
      return false;
    }
  }
  
  public boolean makeThumbs( int iSize ) {
    if( isOK() && ( iSize == 110 || iSize == 250) ) {
      
      int h = iSize;
      int w = iSize;
      String sThumbLabel = "";
      
      if( iSize == 110 ) {
        sThumbLabel = "X0110";
      } else {
        sThumbLabel = "X0250";
      }
      
      // Set the size of the short axis of image rectangle
      if( strOrientation.equals("Portrait")) {
        //The width is the short axis
        w = (int) (w * fShortRatio);
      } else if( strOrientation.equals("Landscape")) {
        //The Height is the short axis
        h = (int) (h * fShortRatio);
      }
      // If they are both the same size, let em go.
      
      //BufferedImage i110 = resize(iPicture, 110, 110);
      BufferedImage i110 = getSizedInstance( iPicture, w, h, 
          RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR, true);
      
      String strThumbBase = strDestPath + "/" + sThumbLabel + "/";
      if( ! new File(strThumbBase).exists() ) {
        new File(strThumbBase).mkdirs();
      }
      
      System.out.println(strThumbBase + " should exist now.");
      
      String strFile = strThumbBase + strFileBase + "-" + 
        sThumbLabel + "." + strFileExt;
      
      File fOutfile = new File(strFile);
      
      try {
        if( ! fOutfile.exists() ) {
          
          ImageIO.write(i110, "jpg", fOutfile);
          
          System.out.println(fOutfile + " should exist now.");
        }
      } catch (IOException e) {
        e.printStackTrace();
      }
      
      return true;
      
    } else {
      return false;
    }
  }
  
  /**
   * Convenience method that returns a scaled instance of the
   * provided {@code BufferedImage}.
   *
   * @param img the original image to be scaled
   * @param targetWidth the desired width of the scaled instance,
   *    in pixels
   * @param targetHeight the desired height of the scaled instance,
   *    in pixels
   * @param hint one of the rendering hints that corresponds to
   *    {@code RenderingHints.KEY_INTERPOLATION} (e.g.
   *    {@code RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR},
   *    {@code RenderingHints.VALUE_INTERPOLATION_BILINEAR},
   *    {@code RenderingHints.VALUE_INTERPOLATION_BICUBIC})
   * @param higherQuality if true, this method will use a multi-step
   *    scaling technique that provides higher quality than the usual
   *    one-step technique (only useful in downscaling cases, where
   *    {@code targetWidth} or {@code targetHeight} is
   *    smaller than the original dimensions, and generally only when
   *    the {@code BILINEAR} hint is specified)
   * @return a scaled version of the original {@code BufferedImage}
   */
  private BufferedImage getSizedInstance(BufferedImage img,
                                         int targetWidth,
                                         int targetHeight,
                                         Object hint,
                                         boolean higherQuality)
  {
      int type = (img.getTransparency() == Transparency.OPAQUE) ?
          BufferedImage.TYPE_INT_RGB : BufferedImage.TYPE_INT_ARGB;
      //BufferedImage ret = (BufferedImage)img;
      int w = img.getWidth();
      int h = img.getHeight();
      
      
      /*
       * The original code would alter the image passed in
       * for performance in generating progressively smaller
       * thumbnails.  Sad to say, we are unconcerned with 
       * performance and would prefer that the original image
       * is unaltered - PH (4/19/2011)
       */
      BufferedImage ret = new BufferedImage(w, h, type);
      Graphics2D g2 = ret.createGraphics();
      g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, hint);
      g2.drawImage(img, 0, 0, w, h, null);
      g2.dispose();
      
      if (higherQuality) {
          // Use multi-step technique: start with original size, then
          // scale down in multiple passes with drawImage()
          // until the target size is reached
          w = img.getWidth();
          h = img.getHeight();
      } else {
          // Use one-step technique: scale directly from original
          // size to target size with a single drawImage() call
          w = targetWidth;
          h = targetHeight;
      }
      
      do {
          if (higherQuality && w > targetWidth) {
              w /= 2;
              if (w < targetWidth) {
                  w = targetWidth;
              }
          }

          if (higherQuality && h > targetHeight) {
              h /= 2;
              if (h < targetHeight) {
                  h = targetHeight;
              }
          }

          BufferedImage tmp = new BufferedImage(w, h, type);
          g2 = tmp.createGraphics();
          g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, hint);
          g2.drawImage(ret, 0, 0, w, h, null);
          g2.dispose();

          ret = tmp;
      } while (w != targetWidth || h != targetHeight);

      return ret;
  }
  
  public boolean makeZoom( String sZoomDir ) {
    
    String strFullTargetPath = "";
    File fZoomDir;
    
    if( isOK() ) {
      
      // Get the Zoom base for this image and construct
      // the full path and file name
      if( ! new File(sZoomDir).exists() ) {
        new File(sZoomDir).mkdirs();
      }
      
      fZoomDir = new File(sZoomDir);
      sZoomDir = fZoomDir.getAbsolutePath();
      
      // Create the full path to the target file
      strFullTargetPath = strDestPath + "/" + strFileName;
      
      System.out.println("Trying to link");
      System.out.println(strFullTargetPath);
      System.out.println("to");
      System.out.println(sZoomDir + "/" + strFileName);
      
      Process process;
      try {
        // create a link to the official file from the full path
        process = Runtime.getRuntime().exec( new String[] { "ln", "-s", 
            strFullTargetPath, (sZoomDir + "/" + strFileName) } );
        process.waitFor();
        process.destroy();
        
        // create and execute the zoomify command
        process = Runtime.getRuntime().exec( new String[] { "nice", 
            "python",
            ( LIMSlookup.getZoomExecDir() + "/ZoomifyFileProcessor.py" ), 
            (sZoomDir + "/" + strFileName) } );
        process.waitFor();
        process.destroy();
        
        // Remove the link we made above
        File fTempImage = new File(sZoomDir + "/" + strFileName);
        fTempImage.delete();
        
      } catch (IOException e) {
        e.printStackTrace();
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
      
      return true;
    } else {
      return false;
    }
  }
  
  public String setCreationDate( String strDate ) {
    if( strDate != null && strDate.length() > 0 ) {
      strCreationDate = strDate;
    }
    
    return strCreationDate;
  }
  
  public String getCreationDate() {
    return strCreationDate;
  }
  
  /**
   * @param args
   */
  public static void main(String[] args) {
    // TODO Auto-generated method stub
    LIMSimage thisImage = new LIMSimage("/home/phammer/Temp", 
        "GargoyleSnow.jpg");
    thisImage.makeThumbs(110);
    thisImage.makeThumbs(250);
    thisImage.makeZoom("/home/phammer/Temp/Zoom");
    
    thisImage = new LIMSimage("/home/phammer/Temp", 
    "Brie-1004.jpg");
    thisImage.makeThumbs(110);
    thisImage.makeThumbs(250);
    thisImage.makeZoom("/home/phammer/Temp/Zoom");
    System.out.println("Done!");
  }

}

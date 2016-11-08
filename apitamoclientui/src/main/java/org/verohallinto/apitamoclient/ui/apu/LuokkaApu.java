package org.verohallinto.apitamoclient.ui.apu;

/**
 * <p>
 * </p>
 * (c) 2014 Tietokarhu Oy
 * <p/>
 */
public class LuokkaApu {

  public LuokkaApu() {

  }

  /**
   * Palauttaa luokan ClassLoaderin.
   * </p>
   *
   * @return ClassLoader.
   */
  public ClassLoader annaLataaja() {

    ClassLoader cl = this.getClass().getClassLoader();

    if (cl == null) {
      // work-around set context loader for windows-service started jvms
      // (QUARTZ-748)
      if (Thread.currentThread().getContextClassLoader() == null && this.getClass().getClassLoader() != null) {
        Thread.currentThread().setContextClassLoader(getClass().getClassLoader());
      }

      cl = Thread.currentThread().getContextClassLoader();
    }

    return cl;
  }
}

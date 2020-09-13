import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.TesseractException;
import org.junit.Assert;
import org.junit.Test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

public class HealthManaTest {
    @Test
    public void healthTest() throws IOException, TesseractException {
        InputStream is = getClass().getClassLoader().getResourceAsStream("basic1.jpg");
        assert is != null;
        BufferedImage src = ImageIO.read(is);
        ITesseract instance = Main.getTesseract();

        for (int i = 0; i < 5; i++) {
            HealthManaState result = Main.healthMana(instance, src);
            Assert.assertNotEquals(null, result);
            Assert.assertNotEquals(null, result.health);
            Assert.assertNotEquals(null, result.mana);
            Assert.assertEquals(599, result.health.intValue());
            Assert.assertEquals(250, result.mana.intValue());
        }
    }
}

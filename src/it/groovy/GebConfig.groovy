import org.openqa.selenium.chrome.ChromeDriver
import org.openqa.selenium.chrome.ChromeDriverService
import org.openqa.selenium.chrome.ChromeOptions
import org.openqa.selenium.os.ExecutableFinder

import static org.apache.commons.lang3.SystemUtils.*

File findDriverExecutable() {
    def defaultExecutable = new ExecutableFinder().find("chromedriver")
    if (defaultExecutable) {
        new File(defaultExecutable)
    } else {
        new File("drivers").listFiles().findAll {
            !it.name.endsWith(".version")
        }.find {
            if (IS_OS_LINUX) {
                it.name.contains("linux")
            } else if (IS_OS_MAC) {
                it.name.contains("mac")
            } else if (IS_OS_WINDOWS) {
                it.name.contains("windows")
            }
        }
    }
}

driver = {
    ChromeDriverService.Builder serviceBuilder = new ChromeDriverService.Builder()
            .usingAnyFreePort()
            .usingDriverExecutable(findDriverExecutable())

    if (System.getProperty("headlessChrome", "false") == "true") {
        ChromeOptions chromeOptions = new ChromeOptions()
        chromeOptions.addArguments("--headless")
        chromeOptions.addArguments("--no-sandbox")

        return new ChromeDriver(serviceBuilder.build(), chromeOptions)
    } else {
        return new ChromeDriver(serviceBuilder.build())
    }

}
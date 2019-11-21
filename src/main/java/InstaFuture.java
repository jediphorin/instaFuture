import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.io.*;
import java.util.ArrayList;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.Level;
import java.util.logging.Logger;

import static java.rmi.server.LogStream.log;
import static java.util.Map.entry;

public class InstaFuture {

    private final Map xpaths = Map.ofEntries (
            entry("first_photo",
                    "//*[@id=\"react-root\"]/section/main/div/div[3]/article/div[1]/div/div[1]/div[1]/a/div[1]/div[2]"),

            entry("like_button",
                    "/html/body/div[3]/div[2]/div/article/div[2]/section[1]/span[1]/button/span"),

            entry("geo_label",
                    "/html/body/div[3]/div[2]/div/article/header/div[2]/div[2]/div[2]/a")
    );

    //  управление клавой
    private Robot robot;
    //  список ссылок, где есть мой лайк
    private ArrayList linksList;
    private int baseDelay;
    private String target;
    private String login;
    private String password;
    private static Scanner scanner;
    private ChromeDriver browser;
    private Logger logger;
    private WebElement likeButton;

    //  блок строк для определения того, что мы дошли до конца страницы
    private String urlCurrent;
    private String urlPrevious;
    private String urlFirst;

    private JavascriptExecutor javascriptExecutor;

    InstaFuture(String nick, String code, int baseDelay, String igPage) {
        login = nick;
        password = code;
        target = igPage;
        this.baseDelay = baseDelay;
        logger = Logger.getLogger (InstaFuture.class.getName());
        scanner = new Scanner (System.in);
        javascriptExecutor = (JavascriptExecutor) browser;
        linksList = new ArrayList();
        likeButton = null;

        try {
            robot = new Robot();
        } catch (AWTException e) { e.printStackTrace(); }
    }

    synchronized void start() {
        log ("InstaFuture стартовала!");
        browser = new ChromeDriver();
        login();
        findFirstV2();
        end();
    }

    //  проверка правильности стартовой страницы
    private void checkCorrectUrl() {
        waitASecond();

        if (browser.getCurrentUrl() == "https://www.instagram.com") {
            log ("INSTAGRAM");
            waitASecond();
        }
        else if (browser.getCurrentUrl() == "https://www.instagram.com/475685865") {
            log ("GOVNO!");
            waitASecond();

            for (int i = 0; i < 2; i++) {
                pressTab();
                if (i == 1)
                    pressEnter();
            }
            System.out.println ("tab-tab-enter");

            for (int i = 0; i < 6; i++) {
                System.out.println ("Вводи: ");
                int number = scanner.nextInt();
                if (i == 5)
                    pressEnter();
            }
        }
        else
            System.out.println ("ELSE CHECK");
    }

    private void login() {
        browser.get("https://www.instagram.com/accounts/login/");
        waitASecond();
        System.out.println("123: " + login);
        browser.findElement(new By.ByName("username")).sendKeys (login);
        browser.findElement(new By.ByName ("password")).sendKeys (password);
        pressEnter();
        waitASecond();
        checkCorrectUrl();
        log(String.format("Залогинились, как %s", login));
    }

    //  метод поиска первой фотографии
    private void findFirstV2() {
        browser.get(target);
        log(String.format("ищу свои классы, поставленные для %s", target));
        waitASecond();

        try {
            browser.findElement(new By.ByXPath ((String) xpaths.get("first_photo"))).click();
            waitASecond();
            urlFirst = browser.getCurrentUrl();
            urlCurrent = urlFirst;
            System.out.println(urlFirst + ", " + target);

            //  главный цикл программы
            if (urlFirst != target) {
                urlFirst = null;
                while (urlCurrent != urlPrevious) {
                    waitASecond();
                    geoToFileV2();
                    checkLike();
                    toNext();
                    switchUrls();
                }
                end();
            }

            else if (urlFirst == target) {
                System.out.println("фоток в профиле нет. Завершение работы.");
                end();
            }
        }
        catch (NoSuchElementException ex) {
            warn ("Не могу найти первую фотку! Видимо, фоток нет вообще.");
        }
    }

    //  заменитель линков, чтобы работал механизм определения того, что мы дошли до конца
    private void switchUrls() {
        urlPrevious = urlCurrent;
        urlCurrent = browser.getCurrentUrl();
    }

    //  метод, проверяющий наличие лайка
    private void checkLike() {
        likeButton = null;
        try {
            likeButton = browser.findElement (By.cssSelector (".glyphsSpriteHeart__filled__24__red_5"));
            if (likeButton != null) {
                System.out.println ("лайк есть");
                linksList.add (browser.getCurrentUrl());
                linksToFile(browser.getCurrentUrl() + "\n");
                System.out.println (linksList.size());
            }
        }
        catch (NoSuchElementException e) {}
        finally {
            try {
                likeButton = browser.findElement(By.cssSelector (".glyphsSpriteHeart__outline__24__grey_9"));
            } catch (NoSuchElementException e) {}
        }
    }

    //  метод листания на следующую фотку
    private void toNext() {
        try {
            browser.findElement(new By.ByClassName("coreSpriteRightPaginationArrow")).click();
            waitASecond();
    } catch (NoSuchElementException ex) {
        warn ("Не могу открыть следущее фото. Кажется, закончились.");
    }
}

    //  метод сохранение ссылок в текстовый файл
    private void linksToFile(String url) {
        FileOutputStream fileOutputStream = null;
        try {
            fileOutputStream = new FileOutputStream("links.txt", true);
            fileOutputStream.write(url.getBytes());
            fileOutputStream.flush();
            fileOutputStream.close();
        } catch (IOException e) { e.printStackTrace(); }
    }

    //  метод, сохраняющий геометку в текстовый файл
    private void geoToFileV2() {
        try {
            WebElement geo = browser.findElement(By.cssSelector(".O4GlU"));
            System.out.println("геометка есть");
            System.out.println("geo: " + geo.getAttribute("innerHTML"));
            String geoString = geo.getAttribute("innerHTML") + "\n";
            geo = null;
            FileOutputStream fileOutputStream = null;
            try {
                fileOutputStream = new FileOutputStream("geo.txt", true);
                fileOutputStream.write(geoString.getBytes());
                fileOutputStream.flush();
                fileOutputStream.close();
            } catch (IOException e) { e.printStackTrace(); }
        } catch (NoSuchElementException e) { System.out.println("геометки нет"); }
    }

        public void end() {
            log("Завершение");
            browser.close();
        }

        //  метод паузы рандомного размера
        private void waitASecond() {
            try {
                Thread.sleep ((baseDelay + ThreadLocalRandom.current().nextInt (1, 3)) * 1000);
            } catch (InterruptedException ex) { ex.printStackTrace(); }
        }
        private void pressTab() {
            robot.keyPress (KeyEvent.VK_TAB);
            robot.keyRelease (KeyEvent.VK_TAB);
        }
        private void pressEnter() {
            robot.keyPress(KeyEvent.VK_ENTER);
            robot.keyRelease(KeyEvent.VK_ENTER);
        }

        private void log (String message) { logger.log (Level.INFO, String.format ("Instalike: %s", message)); }
        private void warn (String message) { logger.log (Level.WARNING, String.format ("Instalike: %s", message)); }

        public int getBaseDelay() { return baseDelay; }
    }
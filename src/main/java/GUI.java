import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class GUI extends JFrame {
    InstaFuture logic = null;
    private String nick;
    private String password;
    private String igPage;
    private boolean flagToStart;

    public GUI() {
        setTitle("instaFuture");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setBounds(300, 300, 400, 200);
        setLayout(new BoxLayout(getContentPane(), BoxLayout.X_AXIS));

        JPanel[] mainPanels = new JPanel[2];
        for (int i = 0; i < mainPanels.length; i++) {
            mainPanels[i] = new JPanel();
            add(mainPanels[i]);
            mainPanels[i].setBackground(new Color(100 + i * 40, 100 + i * 40, 100 + i * 40));
        }

        JLabel[] jLabels = new JLabel[3];
        String[] textFieldLabels = new String[3];
        textFieldLabels[0] = "ник страницы-инспектора:";
        textFieldLabels[1] = "пароль:";
        textFieldLabels[2] = "ник изучаемого инстаграм-профиля:";
        JTextField[] jTextFields = new JTextField[3];
        for (int i = 0; i < textFieldLabels.length; i++) {
            jLabels[i] = new JLabel(textFieldLabels[i]);
            jTextFields[i] = new JTextField();
        }

        String[] nameOfButton = new String[4];
        nameOfButton[0] = "войти";
        nameOfButton[1] = "стоп";
        nameOfButton[2] = "старт";
        nameOfButton[3] = "выйти";
        JButton[] jButtons = new JButton[4];
        for (int i = 0; i < nameOfButton.length; i++)
            jButtons[i] = new JButton(nameOfButton[i]);

        mainPanels[0].setLayout(new GridLayout(3, 1));
        mainPanels[1].setLayout(new GridLayout(4, 1));

        JPanel[] leftPanels = new JPanel[3];
        for (int i = 0; i < leftPanels.length; i++) {
            leftPanels[i] = new JPanel();
            mainPanels[0].add(leftPanels[i]);
            leftPanels[i].setLayout(new BoxLayout(leftPanels[i], BoxLayout.Y_AXIS));
            leftPanels[i].add(jLabels[i]);
            leftPanels[i].add(jTextFields[i]);
        }

        for (int i = 0; i < 4; i++)
            mainPanels[1].add(jButtons[i]);

        //  слушатель кнопки "войти"
        jButtons[0].addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                nick = jTextFields[0].getText();
                password = jTextFields[1].getText();
                igPage = jTextFields[2].getText();
                System.out.println("nick = " + nick + "\tpassword = " + password + "\ttarget = " + igPage);
                flagToStart = true;
                enter();
            }
        });

        jButtons[3].addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                System.out.println("третья кнопка");
                System.exit(0);
                logic.end();
            }
        });

        setVisible(true);
    }

    //  метод запуска новой логики проверки (возможно будет запускать больше, чем одно окно)
    private void enter() {
        do {
            System.out.println(flagToStart);
            logic = new InstaFuture(nick, password, 3, "http://www.instagram.com/" + igPage);
        } while (!flagToStart);
        logic.start();
    }

    public String getNick() { return nick; }
    public String getPassword() { return password; }
    public String getIgPage() { return igPage; }
    public boolean isFlagToStart() { return flagToStart; }
}
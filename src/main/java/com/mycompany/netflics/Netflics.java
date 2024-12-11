package com.mycompany.netflics;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.ReentrantLock;

/**
 *
 */
public class Netflics extends JFrame {

    private static final int NUM_THREADS = 5; // Number of threads

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new Netflics().setVisible(true);
            }
        });
    }

    public Netflics() {
        setTitle("Movie Search App");
        setSize(500, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        MovieDatabase movieDatabase = new MovieDatabase();

        JLabel instructionLabel = new JLabel("Enter the name of the movie or show:");
        instructionLabel.setFont(new Font("Arial", Font.BOLD, 16));
        instructionLabel.setForeground(new Color(80, 80, 80));

        JTextField searchTextField = new JTextField(20);
        searchTextField.setFont(new Font("Arial", Font.PLAIN, 14));
        searchTextField.setForeground(Color.DARK_GRAY);
        searchTextField.setBackground(new Color(240, 240, 240));
        searchTextField.setBorder(BorderFactory.createLineBorder(new Color(100, 100, 255), 2));

        JButton searchButton = new JButton("Search");
        searchButton.setFont(new Font("Arial", Font.BOLD, 14));
        searchButton.setBackground(new Color(100, 100, 255));
        searchButton.setForeground(Color.WHITE);
        searchButton.setFocusPainted(false);
        searchButton.setPreferredSize(new Dimension(100, 40));
        searchButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        searchButton.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JTextArea resultTextArea = new JTextArea(10, 30);
        resultTextArea.setFont(new Font("Arial", Font.PLAIN, 14));
        resultTextArea.setForeground(Color.BLACK);
        resultTextArea.setBackground(new Color(240, 240, 240));
        resultTextArea.setEditable(false);
        resultTextArea.setBorder(BorderFactory.createLineBorder(new Color(100, 100, 255), 2));
        resultTextArea.setLineWrap(true);
        resultTextArea.setWrapStyleWord(true);
        JScrollPane scrollPane = new JScrollPane(resultTextArea);

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(new Color(245, 245, 245));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        panel.add(instructionLabel);
        panel.add(Box.createRigidArea(new Dimension(0, 10)));
        panel.add(searchTextField);
        panel.add(Box.createRigidArea(new Dimension(0, 10)));
        panel.add(searchButton);
        panel.add(Box.createRigidArea(new Dimension(0, 20)));
        panel.add(scrollPane);

        add(panel);

        searchButton.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                String searchTerm = searchTextField.getText().trim();
                if (!searchTerm.isEmpty()) {
                    performSearchAsync(movieDatabase, searchTerm, resultTextArea);
                } else {
                    resultTextArea.setText("Please enter a search term.");
                }
            }
        });
    }

    private void performSearchAsync(final MovieDatabase movieDatabase, final String searchTerm, final JTextArea resultTextArea) {

        SwingWorker<String, Void> searchWorker = new SwingWorker<String, Void>() {
            @Override
            protected String doInBackground() throws Exception {
                Movie result = movieDatabase.search(searchTerm);
                if (result == null) {
                    return "No movie or show found with that name.";
                }
                return "Title: " + result.getTitle() + "\n"
                        + "Description: " + result.getDescription() + "\n"
                        + "Director: " + result.getDirector();
            }

            @Override
            protected void done() {
                try {
                    resultTextArea.setText(get());
                } catch (Exception e) {
                    resultTextArea.setText("There was an error performing the search.");
                }
            }
        };

        searchWorker.execute();
    }

    public static class Movie {

        private String title;
        private String description;
        private String director;

        public Movie(String title, String description, String director) {
            this.title = title;
            this.description = description;
            this.director = director;
        }

        public String getTitle() {
            return title;
        }

        public String getDescription() {
            return description;
        }

        public String getDirector() {
            return director;
        }
    }

    public static class MovieDatabase {

        private final ConcurrentHashMap<String, Movie> movieData;
        private final ReentrantLock lock = new ReentrantLock();

        public MovieDatabase() {
            movieData = new ConcurrentHashMap<>();
            movieData.put("inception", new Movie("Inception",
                    "A mind-bending thriller by Christopher Nolan.",
                    "Christopher Nolan"));
            movieData.put("the matrix", new Movie("The Matrix",
                    "A computer hacker learns about the true nature of his reality.",
                    "The Wachowskis"));
            movieData.put("the dark knight", new Movie("The Dark Knight",
                    "Batman faces off against the Joker, a villain who seeks to create chaos.",
                    "Christopher Nolan"));
            movieData.put("interstellar", new Movie("Interstellar",
                    "A team of explorers travel through a wormhole in space in search of a new home for humanity.",
                    "Christopher Nolan"));
            movieData.put("stranger things", new Movie("Stranger Things",
                    "A group of kids uncover supernatural events in their small town.",
                    "The Duffer Brothers"));
            movieData.put("breaking bad", new Movie("Breaking Bad",
                    "A high school chemistry teacher turned methamphetamine producer.",
                    "Vince Gilligan"));
        }

        public Movie search(String term) {
            try {
                lock.lock();
                return movieData.get(term.toLowerCase());
            } finally {
                lock.unlock();
            }
        }
    }
}


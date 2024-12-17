package com.mycompany.netflics;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.io.*;
import java.util.*;
import java.util.concurrent.*;

import javax.swing.*;

public class Netflics extends JFrame {

    public static void main(String[] args) {
        new Netflics().setVisible(true); // inicia la aplicación mostrando la pantalla de netflcis
    }

    public Netflics() {
        setTitle("Movie Search App"); // titulo de la ventana
        setSize(750, 750);// tamaño
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);// cierra la aplicación al cerrar la ventana
        setLocationRelativeTo(null);//centrar la venta
        setResizable(false); // no es redimensional

        MovieDatabase movieDatabase = new MovieDatabase();// inicializa la base de datos de peliculas
        //etiqueta de instrucción
        JLabel instructionLabel = new JLabel("Enter the name of the movie or show:");
        instructionLabel.setFont(new Font("Arial", Font.BOLD, 16));
        instructionLabel.setForeground(new Color(80, 80, 80));
            // campo de texto para la busqueda
        JTextField searchTextField = new JTextField(20);
        searchTextField.setFont(new Font("Arial", Font.PLAIN, 14));
        searchTextField.setForeground(Color.DARK_GRAY);
        searchTextField.setBackground(new Color(240, 240, 240));
        searchTextField.setBorder(BorderFactory.createLineBorder(new Color(100, 100, 255), 2));
        // oton de buscar
        JButton searchButton = new JButton("Search");
        searchButton.setFont(new Font("Arial", Font.BOLD, 14));
        searchButton.setBackground(new Color(100, 100, 255));
        searchButton.setForeground(Color.WHITE);
        searchButton.setFocusPainted(false);
        searchButton.setPreferredSize(new Dimension(100, 40));
        searchButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        searchButton.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        // area de texto para mostrar resultados
        JTextArea resultTextArea = new JTextArea(10, 30);
        resultTextArea.setFont(new Font("Arial", Font.PLAIN, 14));
        resultTextArea.setForeground(Color.BLACK);
        resultTextArea.setBackground(new Color(240, 240, 240));
        resultTextArea.setEditable(false);
        resultTextArea.setBorder(BorderFactory.createLineBorder(new Color(100, 100, 255), 2));
        resultTextArea.setLineWrap(true);
        resultTextArea.setWrapStyleWord(true);
        JScrollPane resultScrollPane = new JScrollPane(resultTextArea);
        //  eitqueta para recomendaciones
        JLabel relatedLabel = new JLabel("Content you may like:");
        relatedLabel.setFont(new Font("Arial", Font.BOLD, 16));
        relatedLabel.setForeground(new Color(80, 80, 80));
        //mostrar recomendaciones
        JTextArea relatedTextArea = new JTextArea(10, 30);
        relatedTextArea.setFont(new Font("Arial", Font.PLAIN, 14));
        relatedTextArea.setForeground(Color.BLACK);
        relatedTextArea.setBackground(new Color(240, 240, 240));
        relatedTextArea.setEditable(false);
        relatedTextArea.setBorder(BorderFactory.createLineBorder(new Color(100, 100, 255), 2));
        relatedTextArea.setLineWrap(true);
        relatedTextArea.setWrapStyleWord(true);
        JScrollPane relatedScrollPane = new JScrollPane(relatedTextArea);
        // panel principal
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(new Color(245, 245, 245));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        //añado los componentes al panel principal
        panel.add(instructionLabel);
        panel.add(Box.createRigidArea(new Dimension(0, 10)));
        panel.add(searchTextField);
        panel.add(Box.createRigidArea(new Dimension(0, 10)));
        panel.add(searchButton);
        panel.add(Box.createRigidArea(new Dimension(0, 20)));
        panel.add(resultScrollPane);
        panel.add(Box.createRigidArea(new Dimension(0, 20)));
        panel.add(relatedLabel);
        panel.add(Box.createRigidArea(new Dimension(0, 10)));
        panel.add(relatedScrollPane);

        add(panel);// añadir el panel a la ventana
        //accion del boton de busqueda
        searchButton.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                String searchTerm = searchTextField.getText().trim();//obtiene el termino de busqueda
                if (!searchTerm.isEmpty()) {
                    performSearch(movieDatabase, searchTerm, resultTextArea, relatedTextArea); // realizar busqueda en la base de datos
                } else {
                    resultTextArea.setText("Please enter a search term.");// esta vacio
                }
            }
        });
    }

    private void performSearch(MovieDatabase movieDatabase, String searchTerm, JTextArea resultTextArea,
            JTextArea relatedTextArea) {
        Thread searchThread = new Thread(new Runnable() {
            @Override
            public void run() {
                Movie result = movieDatabase.search(searchTerm); // busca la pelicula
                if (result == null) {
                    resultTextArea.setText("No movie or show found with that name.");//mensaje sino no encuentra
                } else {
                    resultTextArea.setText("Title: " + result.getTitle() + "\n"
                            + "Description: " + result.getDescription() + "\n"
                            + "Director: " + result.getDirector());// mostrar la información de la pelicula
                    updateMetadata("files/metadata.txt", result);// actualizo

                    MovieGenre frequentGenre = findRelatedMovies("files/metadata.txt");// encuentra genero relacionado

                    Movie relatedMovie = movieDatabase.searchByGenre(frequentGenre);//busca peliculas relacionadas

                    if (relatedMovie == null) {
                        relatedMovie = movieDatabase.getRandomMovie();//obtener una pelicula aleatorio si no hay relacion
                    }

                    relatedTextArea.setText("Recommended: " + relatedMovie.getTitle() + "\n"
                            + "Genre: " + relatedMovie.getGenre());// mostrar recomendación
                }
            }
        });
        searchThread.start();// iniciar el hilo de busqueda
    }

    public static void updateMetadata(String filePath, Movie foundMovie) {
        try {
            // Set para comprobar que los títulos sean únicos (Set no admite repetidos)
            Set<String> titulosPeliculas = new HashSet<>();
            File file = new File(filePath);
            // Si no existe el archivo que tiene info del usuario, lo crea..
            if (!file.exists()) {
                file.createNewFile();
            }
            // [!] Pongo el br aquí abajo en lugar de en try() porque file.createNewFile() tiene que ocurrir antes
            //     de leer y debe estar recogida por si da error, lo que implicaría un try dentro de un try.
            //     Prefiero cerrar el buffer cuando se necesite.

            BufferedReader br = new BufferedReader(new FileReader(file));
            String linea;

            while ((linea = br.readLine()) != null) {
                  // Lee linea, parte por ";", coge el primer segmento que debe
                // tener titulo (dato único)...
                String[] campos = linea.split(";");
                if (campos.length > 0) {
                    // ¿Hay por lo menos 1 trozo? Podemos coger, no peta
                    // Todo bien, al HashSet
                    titulosPeliculas.add(campos[0].trim());
                }
            }
            // ¿La película pasada ya está en el archivo (guardado en HashSet)?
            if (titulosPeliculas.contains(foundMovie.getTitle())) {
                // ¿Sí? Pasamos
                System.out.println("La película '" + foundMovie.getTitle() + "' ya existe en el archivo.");
            } else {
                 // ¿No? Añadimos al final
                try (BufferedWriter bw = new BufferedWriter(new FileWriter(file, true))) {
                    bw.write(foundMovie.toString());
                    bw.newLine();
                    System.out.println("Película '" + foundMovie.getTitle() + "' añadida correctamente.");
                }
            }

            br.close();
        } catch (FileNotFoundException ex) {
            System.out.println("Error encontrando el fichero: " + ex.getMessage());
        } catch (IOException ex) {
            System.out.println("Error manejando metadata: " + ex.getMessage());
        }
    }

    private static final int NUM_THREADS = 4; // numeros de hilos
    private static Semaphore semaphore = new Semaphore(1);  // Semáforo para controlar el acceso

    private MovieGenre findRelatedMovies(String filePath) {
       // ------ Leer metadata y sacar los géneros ------
        ExecutorService executor = Executors.newFixedThreadPool(NUM_THREADS);
        ConcurrentHashMap<MovieGenre, Integer> genreCount = new ConcurrentHashMap<>();

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {

            List<String> lines = new ArrayList<>();
            String line;
        // Saco todas las líneas a lista (para dividirlas luego)
            while ((line = br.readLine()) != null) {
                lines.add(line);
            }
            // Calcular cuántas líneas deberá leer cada hilo
            int linesPerThread = lines.size() / NUM_THREADS;
            // Crear y enviar tareas a los hilos
            for (int i = 0; i < NUM_THREADS; i++) {
            // Calculo el nº de líneas que se va a encargar cada uno
                final int startIndex = i * linesPerThread;
                final int endIndex = (i == NUM_THREADS - 1) ? lines.size() : (i + 1) * linesPerThread;
                  // ^ Resumen:
                // Condición ? Solución1 : Solución2
                // i =/= NUM_THREADS - 1 -> Queda archivo, leemos las líneas calculadas por
                // defecto
                // i == NUM_THREADS - 1 -> Vamos a llegar al final, leemos hasta el total

                executor.submit(() -> {
                    // Cada hilo lee línea, parte por ";", coge campo género y lo añade al
                    // ConcurrentHashmap
                    try {
                        for (int j = startIndex; j < endIndex; j++) {
                            String[] fields = lines.get(j).split(";");
                            if (fields.length > 3) {
                                MovieGenre genre = MovieGenre.valueOf(fields[3].toUpperCase());

                                semaphore.acquire();  // Adquirir el semáforo antes de modificar el mapa
                                try {
                                    
                                    genreCount.merge(genre, 1, Integer::sum);
                                    System.out.println("¡Hilo ha encontrado película del género " + genre + "!");
                                } finally {
                                    semaphore.release();  // Liberar el semáforo después de modificar el mapa
                                }
                            }
                        }
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                });
            }
            // no se aceptan mas tareas
            executor.shutdown();
            while (!executor.isTerminated()) {
                //esperar a q todos los hilos terminen
            }
            // ---------- Calcular género preferido ----------
            MovieGenre mostFrequentGenre = null;
            int maxCount = 0;
            // Coge un género, saca cuantos hay en total en la lista
            // ¿Mayor al máximo del anterior? Es el nuevo preferido
            for (MovieGenre genre : genreCount.keySet()) {
                int count = genreCount.get(genre);
                if (count > maxCount) {
                    mostFrequentGenre = genre;
                    maxCount = count;
                }
            }

            // Si no encuentra género frecuente (porque ha desaparecido el
            // archivo metadata, por ejemplo), pone como género preferido acción
            if (mostFrequentGenre == null) {
                return MovieGenre.ACTION;
            }

            return mostFrequentGenre;

        } catch (FileNotFoundException ex) {
            System.out.println("No se ha encontrado el fichero metadata: " + ex.getMessage());
        } catch (IOException e) {
            System.out.println("Error leyendo el fichero de metadata: " + e.getMessage());
        }
    // Ha dado error, valor genérico
        return MovieGenre.values()[0];
    }
    // Guardo todos los géneros admitidos en un enum......

    public static enum MovieGenre {
        ACTION, COMEDY, MYSTERY, DRAMA, SCIFI, FANTASY, HORROR, ROMANCE,
    }

    public static class Movie {

        private String title;
        private String description;
        private String director;
        private MovieGenre genre;

        public Movie(String title, String description, String director, MovieGenre genre) {
            this.title = title;
            this.description = description;
            this.director = director;
            this.genre = genre;
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

        public MovieGenre getGenre() {
            return genre;
        }

        @Override
        public String toString() {
            return title + ";" + description + ";" + director + ";" + genre;
        }
    }

    public static class MovieDatabase {
        // ConcurrentHashMap con todas las películas/series disponibles

        private final ConcurrentHashMap<String, Movie> movieData;
        // Añadir películas/series de prueba al mapa

        public MovieDatabase() {
            movieData = new ConcurrentHashMap<>();
            movieData.put("inception", new Movie("Inception", "A mind-bending thriller by Christopher Nolan.", "Christopher Nolan", MovieGenre.MYSTERY));
            movieData.put("the matrix", new Movie("The Matrix", "A computer hacker learns about the true nature of his reality.", "The Wachowskis", MovieGenre.ACTION));
            movieData.put("the dark knight", new Movie("The Dark Knight", "Batman faces off against the Joker, a villain who seeks to create chaos.", "Christopher Nolan", MovieGenre.ACTION));
            movieData.put("interstellar", new Movie("Interstellar", "A team of explorers travel through a wormhole in space in search of a new home for humanity.", "Christopher Nolan", MovieGenre.SCIFI));
            movieData.put("stranger things", new Movie("Stranger Things", "A group of kids uncover supernatural events in their small town.", "The Duffer Brothers", MovieGenre.FANTASY));
            movieData.put("breaking bad", new Movie("Breaking Bad", "A high school chemistry teacher turned methamphetamine producer.", "Vince Gilligan", MovieGenre.ACTION));
            movieData.put("the notebook", new Movie("The Notebook", "A love story that spans decades, starring Ryan Gosling and Rachel McAdams.", "Nick Cassavetes", MovieGenre.ROMANCE));
            movieData.put("superbad", new Movie("Superbad", "A hilarious comedy about high school friends trying to have a good time.", "Greg Mottola", MovieGenre.COMEDY));
            movieData.put("the shawshank redemption", new Movie("The Shawshank Redemption", "A gripping drama about a man wrongly convicted of murder and his life in prison.", "Frank Darabont", MovieGenre.DRAMA));
            movieData.put("the conjuring", new Movie("The Conjuring", "A chilling horror movie based on true events of paranormal investigators Ed and Lorraine Warren.", "James Wan", MovieGenre.HORROR));
        }
        // Devolver objeto película que tenga el mismo titulo

        public Movie search(String term) {
            return movieData.get(term.toLowerCase());
        }
        // Buscar por género (para películas relacionadas)

        public Movie searchByGenre(MovieGenre genre) {
            for (Movie movie : movieData.values()) {
                if (movie.getGenre() == genre) {
                    return movie;
                }
            }
         // No ha encontrado otra con el mismo genero, nulo

            return null;
        }
        // Devuelve un objeto Movie aleatorio (para películas relacionadas)

        
        
        public Movie getRandomMovie() {
            Object[] movies = movieData.values().toArray();
            return (Movie) movies[(int) (Math.random() * movies.length)];
        }
    }
}


# Netflics

Netflics es un buscador de películas y series, ofreciendo recomendaciones en relación a los géneros más vistos por el usuario

![alt text](https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcRKfw3WnsVEiW4iZ7TwQBSegnZ1fuhCpHCtXg&s)

## Componentes

- [@lcalorg3006](https://github.com/lcalorg3006)
- [@BGarciaQuesada](https://github.com/BGarciaQuesada)
- [@AContreras2K](https://github.com/Acontreras2k)

## Estructura concurrente

Este proyecto hace uso de ConcurrentHashMap.

## Descripción de la estructura

### Introducción

En lo más básico, es una tabla hash, una estructura de datos para almacenar grandes cantidades de información sobre la que se va a operar.

El ConcurrentHashMap mantiene las funcionalidades del HashMap, con la diferencia de estar preparada para la concurrencia. 

En la recuperación de datos, soporta la concurrencia enteramente. Por otro lado, hay un gran expectativa de concurrencia en las actualizaciones. 

### Casos de uso

Al tener métodos de búsqueda que lanzan varios hilos para agilizar el proceso, es necesario para que cuando guarden lo encontrado no se pisen.

### Métodos de interés

| **Método** | **Descripción** |
|:---:|:---:|
| performSearch | Llama a buscar para mostrar por pantalla una película o serie.  En caso exitoso, actualiza el archivo metadata. |
| updateMetadata | Ejecutado en caso de búsqueda exitosa. Añade al archivo  metadata.txt la película buscada como string si es la  primera vez que se busca. Si no existe dicho archivo, lo crea.  |
| findRelatedMovies | Encuentra cuáles géneros aparecen más en metadata.txt y devuelve una película del mismo tipo. Si no se encuentran preferencias, devuelve género acción. |

## Ficheros

- **metadata.txt**: información obtenida de las búsquedas del usuario. Contiene todas las películas que ha buscado.
# Play Your List

Play Your List is a playlist manager for companies running HPWM (High Productivity With Music). People build playlists out of a shared song catalog, and the system keeps track of how many times each playlist gets played.

The application is split into four services: musicas, playlists, reproducoes and api. In a real deployment each one would be its own project. For this assignment they all live inside a single Spring Boot application, under `playlist/`.

## The four services

**musicas** owns the song catalog: title, artist, album, duration in seconds and genre. It is a plain CRUD.

**playlists** owns the playlists and the link between a playlist and its songs. It creates, renames and deletes playlists, and adds or removes songs from them.

**reproducoes** records plays. Every execution writes a row with the playlist id and a timestamp, so you can ask how many times a playlist has been played.

**api** is the orchestrator. It never touches the database. It calls the other three services over HTTP with Open Feign, checks that the resources exist before doing anything, and answers with a readable message.

## Running it

```bash
cd playlist
./mvnw spring-boot:run
```

On Windows:

```bash
.\mvnw.cmd spring-boot:run
```

Everything answers on port 8080. The database is H2 kept in memory, so the app starts fresh every time with the data loaded from `data.sql`. You can browse it at `http://localhost:8080/h2-console` with the JDBC URL `jdbc:h2:mem:playlistdb`, user `sa`, password `password`. There is a health check at `http://localhost:8080/actuator/health`.

To try the endpoints, open `playlist/requests.http` in VS Code, or import `playlist/thunder-collection_play-your-list.json` into Thunder Client.

## Endpoints

Songs:

```
POST   /musicas               create a song
GET    /musicas               list every song
GET    /musicas/{id}          get one song
PUT    /musicas/{id}          update a song
DELETE /musicas/{id}          delete a song
```

Playlists:

```
POST   /playlists                                  create a playlist
GET    /playlists                                  list every playlist
GET    /playlists/{playlistid}                     get one playlist
PUT    /playlists/{playlistid}                     update name and description
DELETE /playlists/{playlistid}                     delete the playlist and its song links
POST   /playlists/{playlistid}/musicas/{musicaId}  add a song to the playlist
DELETE /playlists/{playlistid}/musicas/{musicaId}  remove a song from the playlist
GET    /playlists/{playlistid}/musicas             list the song ids in the playlist
```

Plays:

```
POST   /reproducao                   register one play of a playlist
GET    /reproducao/{playlistid}      list the plays of a playlist
GET    /reproducao/total/{playlistid}   how many times it was played
```

The timestamp always comes from the server, so the request body only needs the playlist id. The same registration also answers at `POST /statistic`, which is the path the api service calls.

Orchestrator:

```
POST   /api/adicionar/{playlistId}/musicas/{musicaId}
PUT    /api/executar/{playlistId}
```

The first one validates both the playlist and the song through Open Feign, adds the song, and returns something like `Música Imagine adicionada com sucesso à playlist Música Brasileira`. The second one plays a playlist, registering it through `POST /statistic`. If either resource does not exist, nothing is written and you get a 404.

## Validation

A song needs a title and an artist, neither of them empty once you trim the spaces, and a duration greater than zero. Album is optional but stops at 150 characters, genre is optional but stops at 50.

A playlist needs a name, also not empty once trimmed. Description is optional and stops at 255 characters.

A failed validation returns 400 with one message per field:

```json
{
  "titulo": "Título é obrigatório",
  "duracao": "Duração deve ser maior que zero"
}
```

A resource that does not exist returns 404:

```json
{ "erro": "Playlist não localizada (playlistid = 999)" }
```

Deleting a song that still belongs to some playlist returns 409. Remove it from the playlists first, or delete the playlist, which also clears its song links.

## Tests

```bash
cd playlist
./mvnw test
```

27 tests cover the CRUD operations, every validation rule, the 404 and 409 cases, the cascading playlist delete, and the orchestration done by api with the Feign clients mocked. Each test reloads `data.sql`, so all of them start from the same data.

## Layout

```
playlist/src/main/java/music/playlist
  PlaylistApplication.java   entry point, enables the Feign clients
  musicas/                   song catalog
  playlists/                 playlists and their song links
  reproducoes/               play history
  api/                       orchestrator and Feign clients
  excecoes/                  business exceptions and the global error handler

playlist/src/main/resources
  application.properties
  data.sql                   tables and sample data
```

## Stack

Java 21, Spring Boot 4.1.1, Spring Web, Spring Data JPA, Bean Validation, Actuator, H2 and Spring Cloud OpenFeign.

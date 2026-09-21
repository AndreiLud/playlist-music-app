package music.playlist.musicas;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Cada teste recarrega o data.sql, portanto parte sempre da carga inicial
 * (músicas de id 1 a 5 e playlists de id 1 a 5).
 */
@SpringBootTest
@AutoConfigureMockMvc
@Sql("/data.sql")
class MusicaControllerTests {

    private static final String HOTEL_CALIFORNIA = """
            {"titulo":"Hotel California","artista":"Eagles","album":"Hotel California","duracao":391,"genero":"Rock"}
            """;

    @Autowired
    private MockMvc mvc;

    @Test
    @DisplayName("GET /musicas retorna as músicas da carga inicial")
    void listarRetornaCargaInicial() throws Exception {
        mvc.perform(get("/musicas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(5))
                .andExpect(jsonPath("$[0].titulo").value("Imagine"))
                .andExpect(jsonPath("$[0].duracao").value(183));
    }

    @Test
    @DisplayName("GET /musicas/{id} retorna a música e 404 quando não existe")
    void buscarPorId() throws Exception {
        mvc.perform(get("/musicas/2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.artista").value("Michael Jackson"));

        mvc.perform(get("/musicas/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.erro").exists());
    }

    @Test
    @DisplayName("POST /musicas cadastra e devolve 201 com o id gerado")
    void cadastrar() throws Exception {
        mvc.perform(post("/musicas").contentType(MediaType.APPLICATION_JSON).content(HOTEL_CALIFORNIA))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(6));

        mvc.perform(get("/musicas")).andExpect(jsonPath("$.length()").value(6));
    }

    @Test
    @DisplayName("POST /musicas aplica as validações de título, artista e duração")
    void cadastrarComDadosInvalidos() throws Exception {
        mvc.perform(post("/musicas").contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"titulo":"   ","artista":"","duracao":0}
                        """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.titulo").exists())
                .andExpect(jsonPath("$.artista").exists())
                .andExpect(jsonPath("$.duracao").exists());
    }

    @Test
    @DisplayName("POST /musicas recusa álbum e gênero acima do tamanho máximo")
    void cadastrarComCamposOpcionaisAcimaDoLimite() throws Exception {
        mvc.perform(post("/musicas").contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"titulo":"Teste","artista":"Teste","album":"%s","genero":"%s","duracao":100}
                        """.formatted("A".repeat(151), "B".repeat(51))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.album").exists())
                .andExpect(jsonPath("$.genero").exists());
    }

    @Test
    @DisplayName("PUT /musicas/{id} atualiza todos os campos")
    void atualizar() throws Exception {
        mvc.perform(put("/musicas/1").contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"titulo":"Imagine (Remaster)","artista":"John Lennon","album":"Imagine","duracao":186,"genero":"Rock"}
                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.duracao").value(186));

        mvc.perform(get("/musicas/1")).andExpect(jsonPath("$.duracao").value(186));
    }

    @Test
    @DisplayName("DELETE /musicas/{id} recusa (409) música associada a uma playlist")
    void excluirMusicaAssociada() throws Exception {
        mvc.perform(delete("/musicas/1"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.erro").exists());

        mvc.perform(get("/musicas/1")).andExpect(status().isOk());
    }

    @Test
    @DisplayName("DELETE /musicas/{id} exclui música sem associações e 404 quando não existe")
    void excluirMusicaLivre() throws Exception {
        mvc.perform(post("/musicas").contentType(MediaType.APPLICATION_JSON).content(HOTEL_CALIFORNIA))
                .andExpect(status().isCreated());

        mvc.perform(delete("/musicas/6")).andExpect(status().isNoContent());
        mvc.perform(get("/musicas/6")).andExpect(status().isNotFound());
        mvc.perform(delete("/musicas/6")).andExpect(status().isNotFound());
    }

}

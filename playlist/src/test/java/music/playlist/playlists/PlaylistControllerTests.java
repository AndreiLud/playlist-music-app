package music.playlist.playlists;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
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

@SpringBootTest
@AutoConfigureMockMvc
@Sql("/data.sql")
class PlaylistControllerTests {

    @Autowired
    private MockMvc mvc;

    @Test
    @DisplayName("GET /playlists retorna as playlists da carga inicial")
    void listarRetornaCargaInicial() throws Exception {
        mvc.perform(get("/playlists"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(5))
                .andExpect(jsonPath("$[0].nome").value("Clássicos do Rock"));
    }

    @Test
    @DisplayName("POST /playlists cria a playlist e valida o nome obrigatório")
    void criar() throws Exception {
        mvc.perform(post("/playlists").contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"nome":"Foco Total","descricao":"Playlist para o programa HPWM"}
                        """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(6));

        mvc.perform(post("/playlists").contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"nome":"   ","descricao":"teste"}
                        """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.nome").exists());
    }

    @Test
    @DisplayName("POST /playlists recusa descrição acima de 255 caracteres")
    void criarComDescricaoAcimaDoLimite() throws Exception {
        mvc.perform(post("/playlists").contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"nome":"Teste","descricao":"%s"}
                        """.formatted("A".repeat(256))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.descricao").exists());
    }

    @Test
    @DisplayName("PUT /playlists/{playlistid} atualiza nome e descrição")
    void atualizar() throws Exception {
        mvc.perform(put("/playlists/1").contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"nome":"Rock Classico","descricao":"Atualizada"}
                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Rock Classico"));

        mvc.perform(put("/playlists/999").contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"nome":"Inexistente"}
                        """))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /playlists/{playlistid}/musicas retorna apenas os ids das músicas")
    void listarMusicasDaPlaylist() throws Exception {
        mvc.perform(get("/playlists/1/musicas"))
                .andExpect(status().isOk())
                .andExpect(content().json("[1,3,5]"));

        mvc.perform(get("/playlists/999/musicas")).andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("POST /playlists/{playlistid}/musicas/{musicaId} adiciona sem duplicar")
    void adicionarMusica() throws Exception {
        mvc.perform(post("/playlists/1/musicas/2")).andExpect(status().isCreated());
        mvc.perform(post("/playlists/1/musicas/2")).andExpect(status().isOk());

        mvc.perform(get("/playlists/1/musicas")).andExpect(content().json("[1,3,5,2]"));

        mvc.perform(post("/playlists/1/musicas/999")).andExpect(status().isNotFound());
        mvc.perform(post("/playlists/999/musicas/1")).andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("DELETE /playlists/{playlistid}/musicas/{musicaId} remove a associação")
    void removerMusica() throws Exception {
        mvc.perform(delete("/playlists/1/musicas/3")).andExpect(status().isNoContent());
        mvc.perform(get("/playlists/1/musicas")).andExpect(content().json("[1,5]"));

        // a musica continua cadastrada, apenas saiu da playlist
        mvc.perform(get("/musicas/3")).andExpect(status().isOk());

        // musica que nao esta na playlist
        mvc.perform(delete("/playlists/1/musicas/3")).andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("DELETE /playlists/{playlistid} exclui a playlist e suas músicas associadas")
    void excluirPlaylist() throws Exception {
        mvc.perform(delete("/playlists/1")).andExpect(status().isNoContent());
        mvc.perform(get("/playlists/1")).andExpect(status().isNotFound());
        mvc.perform(delete("/playlists/1")).andExpect(status().isNotFound());

        // as musicas que estavam na playlist continuam cadastradas
        mvc.perform(get("/musicas")).andExpect(jsonPath("$.length()").value(5));
    }

}

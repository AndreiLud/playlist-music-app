package music.playlist.reproducoes;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
class ReproducaoControllerTests {

    @Autowired
    private MockMvc mvc;

    @Test
    @DisplayName("GET /reproducao/{playlistid} lista as execuções da carga inicial")
    void listarReproducoes() throws Exception {
        mvc.perform(get("/reproducao/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(5))
                .andExpect(jsonPath("$[0].playlistid").value(1))
                .andExpect(jsonPath("$[0].datahora").isNotEmpty());
    }

    @Test
    @DisplayName("GET /reproducao/total/{playlistid} retorna o total de execuções")
    void totalDeReproducoes() throws Exception {
        mvc.perform(get("/reproducao/total/1")).andExpect(content().string("5"));
        mvc.perform(get("/reproducao/total/4")).andExpect(content().string("1"));

        // playlist sem execucoes registradas
        mvc.perform(get("/reproducao/total/999")).andExpect(content().string("0"));
    }

    @Test
    @DisplayName("POST /reproducao registra a execução com a data/hora gerada pelo serviço")
    void registrarReproducao() throws Exception {
        mvc.perform(post("/reproducao").contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"playlistid":3}
                        """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.playlistid").value(3))
                .andExpect(jsonPath("$.datahora").isNotEmpty());

        mvc.perform(get("/reproducao/total/3")).andExpect(content().string("3"));
    }

    @Test
    @DisplayName("POST /statistic é o mesmo cadastro, usado pelo microserviço api")
    void registrarReproducaoPeloStatistic() throws Exception {
        mvc.perform(post("/statistic").contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"playlistid":4}
                        """))
                .andExpect(status().isCreated());

        mvc.perform(get("/reproducao/total/4")).andExpect(content().string("2"));
    }

    @Test
    @DisplayName("POST /reproducao exige o playlistid")
    void registrarSemPlaylistid() throws Exception {
        mvc.perform(post("/reproducao").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.playlistid").exists());
    }

}

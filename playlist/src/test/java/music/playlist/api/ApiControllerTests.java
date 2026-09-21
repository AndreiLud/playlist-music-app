package music.playlist.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import feign.FeignException;
import feign.Request;
import feign.RequestTemplate;
import music.playlist.musicas.MusicaEntity;
import music.playlist.playlists.PlaylistEntity;
import music.playlist.reproducoes.ReproducaoEntity;

/**
 * Os clientes Open Feign são substituídos por mocks: o que se verifica aqui é a
 * orquestração feita pelo microserviço api (validação dos dois recursos, chamada
 * ao serviço de destino e mensagem devolvida).
 */
@SpringBootTest
@AutoConfigureMockMvc
class ApiControllerTests {

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private MusicaClienteFeign musicaFeign;

    @MockitoBean
    private PlaylistClienteFeign playlistFeign;

    @MockitoBean
    private ReproducaoClienteFeign reproducaoFeign;

    @Test
    @DisplayName("POST /api/adicionar valida os dois recursos e devolve a mensagem de sucesso")
    void adicionarComSucesso() throws Exception {
        given(playlistFeign.obterPlaylist(2)).willReturn(playlist("Música Brasileira"));
        given(musicaFeign.obterMusica(1)).willReturn(musica("Imagine"));

        MockHttpServletResponse resposta = mvc.perform(post("/api/adicionar/2/musicas/1"))
                .andExpect(status().isOk())
                .andReturn().getResponse();

        assertThat(new String(resposta.getContentAsByteArray(), StandardCharsets.UTF_8))
                .isEqualTo("Música Imagine adicionada com sucesso à playlist Música Brasileira");

        verify(playlistFeign).adicionarMusica(2, 1);
    }

    @Test
    @DisplayName("POST /api/adicionar devolve 404 e não inclui nada quando a playlist não existe")
    void adicionarComPlaylistInexistente() throws Exception {
        given(playlistFeign.obterPlaylist(999)).willThrow(naoLocalizado());

        mvc.perform(post("/api/adicionar/999/musicas/1")).andExpect(status().isNotFound());

        verify(musicaFeign, never()).obterMusica(any());
        verify(playlistFeign, never()).adicionarMusica(any(), any());
    }

    @Test
    @DisplayName("POST /api/adicionar devolve 404 e não inclui nada quando a música não existe")
    void adicionarComMusicaInexistente() throws Exception {
        given(playlistFeign.obterPlaylist(1)).willReturn(playlist("Clássicos do Rock"));
        given(musicaFeign.obterMusica(999)).willThrow(naoLocalizado());

        mvc.perform(post("/api/adicionar/1/musicas/999")).andExpect(status().isNotFound());

        verify(playlistFeign, never()).adicionarMusica(any(), any());
    }

    @Test
    @DisplayName("PUT /api/executar registra a execução no serviço de reproduções")
    void executarPlaylist() throws Exception {
        ReproducaoEntity registro = new ReproducaoEntity(1);
        registro.setDatahora(LocalDateTime.of(2026, 9, 21, 20, 30));

        given(playlistFeign.obterPlaylist(1)).willReturn(playlist("Clássicos do Rock"));
        given(reproducaoFeign.registrar(any(ReproducaoEntity.class))).willReturn(registro);

        MockHttpServletResponse resposta = mvc.perform(put("/api/executar/1"))
                .andExpect(status().isOk())
                .andReturn().getResponse();

        assertThat(new String(resposta.getContentAsByteArray(), StandardCharsets.UTF_8))
                .isEqualTo("Playlist Clássicos do Rock executada com sucesso em 2026-09-21T20:30");

        verify(reproducaoFeign).registrar(any(ReproducaoEntity.class));
    }

    @Test
    @DisplayName("PUT /api/executar devolve 404 e não registra nada quando a playlist não existe")
    void executarPlaylistInexistente() throws Exception {
        given(playlistFeign.obterPlaylist(999)).willThrow(naoLocalizado());

        mvc.perform(put("/api/executar/999")).andExpect(status().isNotFound());

        verify(reproducaoFeign, never()).registrar(any());
    }

    private static PlaylistEntity playlist(String nome) {
        PlaylistEntity playlist = new PlaylistEntity();
        playlist.setNome(nome);
        return playlist;
    }

    private static MusicaEntity musica(String titulo) {
        MusicaEntity musica = new MusicaEntity();
        musica.setTitulo(titulo);
        return musica;
    }

    /** Simula a resposta 404 devolvida pelo serviço remoto ao cliente Feign. */
    private static FeignException.NotFound naoLocalizado() {
        Map<String, Collection<String>> headers = Map.of();
        Request request = Request.create(Request.HttpMethod.GET, "http://localhost", headers, null,
                StandardCharsets.UTF_8, new RequestTemplate());
        return new FeignException.NotFound("404", request, null, headers);
    }

}

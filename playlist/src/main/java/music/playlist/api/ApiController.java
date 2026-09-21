package music.playlist.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import feign.FeignException;
import music.playlist.excecoes.MusicaNaoLocalizadaException;
import music.playlist.excecoes.PlaylistNaoLocalizadaException;
import music.playlist.musicas.MusicaEntity;
import music.playlist.playlists.PlaylistEntity;
import music.playlist.reproducoes.ReproducaoEntity;

/**
 * Microserviço api: orquestra os demais serviços utilizando Open Feign.
 */
@RestController
@RequestMapping("/api")
public class ApiController {

    private static final Logger logger = LoggerFactory.getLogger(ApiController.class);

    private final MusicaClienteFeign musicaFeign;

    private final PlaylistClienteFeign playlistFeign;

    private final ReproducaoClienteFeign reproducaoFeign;

    public ApiController(MusicaClienteFeign musicaFeign, PlaylistClienteFeign playlistFeign,
            ReproducaoClienteFeign reproducaoFeign) {
        this.musicaFeign = musicaFeign;
        this.playlistFeign = playlistFeign;
        this.reproducaoFeign = reproducaoFeign;
    }

    /**
     * POST /api/adicionar/{playlistId}/musicas/{musicaId}
     *
     * Adiciona uma música a uma playlist validando os dois recursos via Open Feign.
     */
    @PostMapping("/adicionar/{playlistId}/musicas/{musicaId}")
    public String adicionar(@PathVariable Integer playlistId, @PathVariable Integer musicaId) {

        // os dois recursos sao validados antes de efetivar a inclusao
        PlaylistEntity playlist = obterPlaylist(playlistId);
        MusicaEntity musica = obterMusica(musicaId);

        playlistFeign.adicionarMusica(playlistId, musicaId);

        String mensagem = "Música " + musica.getTitulo() + " adicionada com sucesso à playlist " + playlist.getNome();
        logger.debug(mensagem);

        return mensagem;
    }

    /**
     * PUT /api/executar/{playlistId}
     *
     * Executa uma playlist gerando um registro de sua execução por meio do
     * endpoint POST /statistic (microserviço reproducoes).
     */
    @PutMapping("/executar/{playlistId}")
    public String executar(@PathVariable Integer playlistId) {

        PlaylistEntity playlist = obterPlaylist(playlistId);

        ReproducaoEntity reproducao = reproducaoFeign.registrar(new ReproducaoEntity(playlistId));

        String mensagem = "Playlist " + playlist.getNome() + " executada com sucesso em " + reproducao.getDatahora();
        logger.debug(mensagem);

        return mensagem;
    }

    private PlaylistEntity obterPlaylist(Integer playlistId) {
        try {
            return playlistFeign.obterPlaylist(playlistId);
        } catch (FeignException.NotFound e) {
            throw new PlaylistNaoLocalizadaException(playlistId);
        }
    }

    private MusicaEntity obterMusica(Integer musicaId) {
        try {
            return musicaFeign.obterMusica(musicaId);
        } catch (FeignException.NotFound e) {
            throw new MusicaNaoLocalizadaException(musicaId);
        }
    }

}

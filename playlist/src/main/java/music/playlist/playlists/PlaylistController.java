package music.playlist.playlists;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import music.playlist.excecoes.MusicaNaoLocalizadaException;
import music.playlist.excecoes.PlaylistNaoLocalizadaException;
import music.playlist.musicas.MusicaRepository;

/**
 * Microserviço playlists: responsável pela manutenção das playlists.
 */
@RestController
@RequestMapping("/playlists")
public class PlaylistController {

    private static final Logger logger = LoggerFactory.getLogger(PlaylistController.class);

    private final PlaylistRepository playlistRepo;

    private final PlaylistMusicaRepository playlistMusicaRepo;

    private final MusicaRepository musicaRepo;

    public PlaylistController(PlaylistRepository playlistRepo, PlaylistMusicaRepository playlistMusicaRepo,
            MusicaRepository musicaRepo) {
        this.playlistRepo = playlistRepo;
        this.playlistMusicaRepo = playlistMusicaRepo;
        this.musicaRepo = musicaRepo;
    }

    // POST /playlists: Criar uma nova playlist
    @PostMapping
    public ResponseEntity<PlaylistEntity> criar(@Valid @RequestBody PlaylistEntity playlist) {
        playlist.setId(null);
        logger.debug("Criando playlist: {}", playlist.getNome());
        return ResponseEntity.status(HttpStatus.CREATED).body(playlistRepo.save(playlist));
    }

    // GET /playlists: Listar todas as playlists
    @GetMapping
    public Iterable<PlaylistEntity> listar() {
        return playlistRepo.findAll();
    }

    // GET /playlists/{playlistid}: Buscar uma playlist pelo playlistid
    @GetMapping("/{playlistid}")
    public PlaylistEntity buscar(@PathVariable Integer playlistid) {
        return playlistRepo.findById(playlistid).orElseThrow(() -> new PlaylistNaoLocalizadaException(playlistid));
    }

    // PUT /playlists/{playlistid}: Atualizar o nome e descrição de uma playlist
    @PutMapping("/{playlistid}")
    public PlaylistEntity atualizar(@PathVariable Integer playlistid, @Valid @RequestBody PlaylistEntity playlist) {

        PlaylistEntity existente = buscar(playlistid);

        existente.setNome(playlist.getNome());
        existente.setDescricao(playlist.getDescricao());

        logger.debug("Atualizando playlist playlistid = {}", playlistid);
        return playlistRepo.save(existente);
    }

    // DELETE /playlists/{playlistid}: Excluir uma playlist e suas músicas associadas
    @DeleteMapping("/{playlistid}")
    @Transactional
    public ResponseEntity<Void> excluir(@PathVariable Integer playlistid) {

        validarPlaylist(playlistid);

        // as associacoes precisam ser removidas antes da playlist (chave estrangeira)
        playlistMusicaRepo.removerTodasDaPlaylist(playlistid);
        playlistRepo.deleteById(playlistid);

        logger.debug("Excluida playlist playlistid = {}", playlistid);
        return ResponseEntity.noContent().build();
    }

    // POST /playlists/{playlistid}/musicas/{musicaId}: Adicionar uma música à playlist
    @PostMapping("/{playlistid}/musicas/{musicaId}")
    public ResponseEntity<PlaylistMusicaEntity> adicionarMusica(@PathVariable Integer playlistid,
            @PathVariable Integer musicaId) {

        validarPlaylist(playlistid);

        if (!musicaRepo.existsById(musicaId)) {
            throw new MusicaNaoLocalizadaException(musicaId);
        }

        // a mesma musica nao e adicionada duas vezes na mesma playlist
        return playlistMusicaRepo.findByPlaylistidAndMusicaid(playlistid, musicaId)
                .map(ResponseEntity::ok)
                .orElseGet(() -> {
                    logger.debug("Musica {} adicionada a playlist {}", musicaId, playlistid);
                    return ResponseEntity.status(HttpStatus.CREATED)
                            .body(playlistMusicaRepo.save(new PlaylistMusicaEntity(playlistid, musicaId)));
                });
    }

    // DELETE /playlists/{playlistid}/musicas/{musicaId}: Remover uma música da playlist
    @DeleteMapping("/{playlistid}/musicas/{musicaId}")
    public ResponseEntity<Void> removerMusica(@PathVariable Integer playlistid, @PathVariable Integer musicaId) {

        validarPlaylist(playlistid);

        if (playlistMusicaRepo.removerDaPlaylist(playlistid, musicaId) == 0) {
            throw new MusicaNaoLocalizadaException(musicaId, playlistid);
        }

        logger.debug("Musica {} removida da playlist {}", musicaId, playlistid);
        return ResponseEntity.noContent().build();
    }

    // GET /playlists/{playlistid}/musicas: Listar os ids das músicas de uma playlist
    @GetMapping("/{playlistid}/musicas")
    public List<Integer> listarMusicas(@PathVariable Integer playlistid) {
        validarPlaylist(playlistid);
        return playlistMusicaRepo.listarMusicaIds(playlistid);
    }

    /** Valida a existência da playlist sem carregar a entidade. */
    private void validarPlaylist(Integer playlistid) {
        if (!playlistRepo.existsById(playlistid)) {
            throw new PlaylistNaoLocalizadaException(playlistid);
        }
    }

}

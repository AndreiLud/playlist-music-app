package music.playlist.musicas;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import music.playlist.excecoes.MusicaEmPlaylistException;
import music.playlist.excecoes.MusicaNaoLocalizadaException;

/**
 * Microserviço musicas: mantém as músicas disponíveis (CRUD).
 */
@RestController
@RequestMapping("/musicas")
public class MusicaController {

    private static final Logger logger = LoggerFactory.getLogger(MusicaController.class);

    private final MusicaRepository musicaRepo;

    public MusicaController(MusicaRepository musicaRepo) {
        this.musicaRepo = musicaRepo;
    }

    // POST /musicas: Cadastrar uma nova música
    @PostMapping
    public ResponseEntity<MusicaEntity> cadastrar(@Valid @RequestBody MusicaEntity musica) {
        musica.setId(null);
        logger.debug("Cadastrando musica: {}", musica.getTitulo());
        return ResponseEntity.status(HttpStatus.CREATED).body(musicaRepo.save(musica));
    }

    // GET /musicas: Listar todas as músicas
    @GetMapping
    public Iterable<MusicaEntity> listar() {
        return musicaRepo.findAll();
    }

    // GET /musicas/{id}: Buscar uma música pelo ID
    @GetMapping("/{id}")
    public MusicaEntity buscar(@PathVariable Integer id) {
        return musicaRepo.findById(id).orElseThrow(() -> new MusicaNaoLocalizadaException(id));
    }

    // PUT /musicas/{id}: Atualizar uma música
    @PutMapping("/{id}")
    public MusicaEntity atualizar(@PathVariable Integer id, @Valid @RequestBody MusicaEntity musica) {

        MusicaEntity existente = buscar(id);

        existente.setTitulo(musica.getTitulo());
        existente.setArtista(musica.getArtista());
        existente.setAlbum(musica.getAlbum());
        existente.setDuracao(musica.getDuracao());
        existente.setGenero(musica.getGenero());

        logger.debug("Atualizando musica id = {}", id);
        return musicaRepo.save(existente);
    }

    // DELETE /musicas/{id}: Excluir uma música
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Integer id) {

        if (!musicaRepo.existsById(id)) {
            throw new MusicaNaoLocalizadaException(id);
        }

        try {
            musicaRepo.deleteById(id);
        } catch (DataIntegrityViolationException e) {
            // a chave estrangeira de playlist_musicas impede a exclusao
            throw new MusicaEmPlaylistException(id);
        }

        logger.debug("Excluida musica id = {}", id);
        return ResponseEntity.noContent().build();
    }

}

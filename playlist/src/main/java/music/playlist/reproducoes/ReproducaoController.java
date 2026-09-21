package music.playlist.reproducoes;

import java.time.LocalDateTime;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

/**
 * Microserviço reproducoes: controla quantas vezes a playlist foi executada.
 *
 * O cadastro é publicado em /reproducao e também em /statistic, que é o nome
 * utilizado pelo microserviço api ao registrar a execução de uma playlist.
 */
@RestController
public class ReproducaoController {

    private static final Logger logger = LoggerFactory.getLogger(ReproducaoController.class);

    private final ReproducaoRepository reproducaoRepo;

    public ReproducaoController(ReproducaoRepository reproducaoRepo) {
        this.reproducaoRepo = reproducaoRepo;
    }

    // POST /reproducao: Cria um registro de reprodução da playlist no banco de dados
    @PostMapping({ "/reproducao", "/statistic" })
    public ResponseEntity<ReproducaoEntity> registrar(@Valid @RequestBody ReproducaoEntity reproducao) {

        reproducao.setId(null);
        // a data/hora da execucao e sempre gerada pelo proprio servico
        reproducao.setDatahora(LocalDateTime.now());

        logger.debug("Registrando reproducao da playlist {}", reproducao.getPlaylistid());
        return ResponseEntity.status(HttpStatus.CREATED).body(reproducaoRepo.save(reproducao));
    }

    // GET /reproducao/{playlistid}: Listar todas as reproduções de uma playlist
    @GetMapping("/reproducao/{playlistid}")
    public List<ReproducaoEntity> listar(@PathVariable Integer playlistid) {
        return reproducaoRepo.findByPlaylistid(playlistid);
    }

    // GET /reproducao/total/{playlistid}: Total de vezes que a playlist foi executada
    @GetMapping("/reproducao/total/{playlistid}")
    public long total(@PathVariable Integer playlistid) {
        return reproducaoRepo.countByPlaylistid(playlistid);
    }

}

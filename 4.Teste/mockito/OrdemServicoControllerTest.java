package com.cartoon.api;

import com.cartoon.api.compartilhado.exceptions.GlobalExceptionHandler;
import com.cartoon.api.compartilhado.exceptions.RecursoNaoEncontradoException;
import com.cartoon.api.ordemServico.controller.OrdemServicoController;
import com.cartoon.api.ordemServico.dto.response.ItemPecaResponse;
import com.cartoon.api.ordemServico.dto.response.ItemServicoResponse;
import com.cartoon.api.ordemServico.dto.response.OrdemServicoHistoricoResponse;
import com.cartoon.api.ordemServico.models.StatusServico;
import com.cartoon.api.ordemServico.service.OrdemServicoService;
import com.cartoon.api.seguranca.SecurityConfig;
import com.cartoon.api.veiculo.dto.response.HistoricoVeiculoResponse;
import com.cartoon.api.veiculo.dto.response.VeiculoResumo;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Testes MockMvc com Mockito para endpoints de Histórico em OrdemServicoController (UC06)
 */
@WebMvcTest(OrdemServicoController.class)
@Import({SecurityConfig.class, GlobalExceptionHandler.class})
public class OrdemServicoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private OrdemServicoService ordemServicoService;

    @Test
    @DisplayName("UC06: GET /api/ordem-servico/historico/{placa} retorna histórico com status 200 OK")
    void deveConsultarHistoricoPorPlaca() throws Exception {
        VeiculoResumo veiculoResumo = new VeiculoResumo(1, "XYZ-9876", "Ka", "Ford", 2019, 35000.0, true, "João da Silva");
        OrdemServicoHistoricoResponse ordem = new OrdemServicoHistoricoResponse(
                10,
                LocalDate.of(2026, 10, 1),
                LocalDate.of(2026, 10, 2),
                LocalDate.of(2026, 10, 3),
                null,
                StatusServico.FINALIZADO,
                "Revisão preventiva",
                "Carlos Mecânico",
                "Oficina Central",
                List.of(new ItemServicoResponse(1, 1, "Troca de óleo", 1, 100.0, 100.0, LocalTime.of(1, 0))),
                List.of(new ItemPecaResponse(1, 1, "Filtro", 1, BigDecimal.valueOf(50.0), BigDecimal.valueOf(50.0))),
                BigDecimal.valueOf(150.0)
        );

        HistoricoVeiculoResponse historico = new HistoricoVeiculoResponse(
                veiculoResumo,
                "Histórico recuperado com sucesso.",
                List.of(ordem),
                1
        );

        when(ordemServicoService.consultarHistoricoPorPlaca("XYZ-9876")).thenReturn(historico);

        mockMvc.perform(get("/api/ordem-servico/historico/XYZ-9876"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.veiculo.placa").value("XYZ-9876"))
                .andExpect(jsonPath("$.mensagem").value("Histórico recuperado com sucesso."))
                .andExpect(jsonPath("$.ordensServico[0].id").value(10))
                .andExpect(jsonPath("$.ordensServico[0].itensServico[0].servicoNome").value("Troca de óleo"))
                .andExpect(jsonPath("$.ordensServico[0].itensPeca[0].pecaNome").value("Filtro"))
                .andExpect(jsonPath("$.ordensServico[0].valorTotal").value(150.0));
    }

    @Test
    @DisplayName("UC06: GET /api/ordem-servico/historico/{placa}/paginado retorna histórico paginado com 200 OK")
    void deveConsultarHistoricoPaginado() throws Exception {
        OrdemServicoHistoricoResponse ordem = new OrdemServicoHistoricoResponse(
                10,
                LocalDate.of(2026, 10, 1),
                null,
                null,
                null,
                StatusServico.PENDENTE,
                "Orçamento inicial",
                "Carlos Mecânico",
                "Oficina Central",
                List.of(),
                List.of(),
                BigDecimal.valueOf(200.0)
        );

        Page<OrdemServicoHistoricoResponse> page = new PageImpl<>(List.of(ordem), PageRequest.of(0, 10), 1);

        when(ordemServicoService.consultarHistoricoPaginadoPorPlaca(eq("XYZ-9876"), any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/api/ordem-servico/historico/XYZ-9876/paginado"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(10))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    @DisplayName("UC06: GET /api/ordem-servico/{id}/detalhado retorna detalhes completos da ordem")
    void deveDetalharOrdem() throws Exception {
        OrdemServicoHistoricoResponse ordem = new OrdemServicoHistoricoResponse(
                10,
                LocalDate.of(2026, 10, 1),
                LocalDate.of(2026, 10, 2),
                LocalDate.of(2026, 10, 3),
                null,
                StatusServico.FINALIZADO,
                "Revisão geral",
                "Carlos Mecânico",
                "Oficina Central",
                List.of(new ItemServicoResponse(1, 1, "Alinhamento", 1, 80.0, 80.0, LocalTime.of(0, 45))),
                List.of(new ItemPecaResponse(1, 1, "Pastilha de freio", 2, BigDecimal.valueOf(60.0), BigDecimal.valueOf(120.0))),
                BigDecimal.valueOf(200.0)
        );

        when(ordemServicoService.detalharOrdem(10)).thenReturn(ordem);

        mockMvc.perform(get("/api/ordem-servico/10/detalhado"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.mecanicoNome").value("Carlos Mecânico"))
                .andExpect(jsonPath("$.status").value("FINALIZADO"))
                .andExpect(jsonPath("$.itensServico[0].servicoNome").value("Alinhamento"))
                .andExpect(jsonPath("$.itensPeca[0].pecaNome").value("Pastilha de freio"))
                .andExpect(jsonPath("$.valorTotal").value(200.0));
    }

    @Test
    @DisplayName("UC06: GET /api/ordem-servico/historico/{placa} para placa inexistente retorna 404")
    void deveRetornar404QuandoPlacaInexistente() throws Exception {
        when(ordemServicoService.consultarHistoricoPorPlaca("NAO-0000"))
                .thenThrow(new RecursoNaoEncontradoException("Veículo não encontrado."));

        mockMvc.perform(get("/api/ordem-servico/historico/NAO-0000"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Veículo não encontrado."));
    }
}

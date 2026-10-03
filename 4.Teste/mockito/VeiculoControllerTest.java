package com.cartoon.api.veiculo;

import com.cartoon.api.compartilhado.exceptions.ConflitoException;
import com.cartoon.api.compartilhado.exceptions.GlobalExceptionHandler;
import com.cartoon.api.compartilhado.exceptions.RecursoNaoEncontradoException;
import com.cartoon.api.ordemServico.dto.response.OrdemServicoHistoricoResponse;
import com.cartoon.api.ordemServico.models.StatusServico;
import com.cartoon.api.ordemServico.service.OrdemServicoService;
import com.cartoon.api.seguranca.SecurityConfig;
import com.cartoon.api.veiculo.dto.request.VeiculoAtualizacaoRequest;
import com.cartoon.api.veiculo.dto.request.VeiculoRequest;
import com.cartoon.api.veiculo.dto.response.HistoricoVeiculoResponse;
import com.cartoon.api.veiculo.dto.response.VeiculoResponse;
import com.cartoon.api.veiculo.dto.response.VeiculoResumo;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Testes de Integração e MockMvc com Mockito para VeiculoController (UC05 e UC06)
 */
@WebMvcTest(VeiculoController.class)
@Import({SecurityConfig.class, GlobalExceptionHandler.class})
public class VeiculoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @MockitoBean
    private VeiculoService veiculoService;

    @MockitoBean
    private OrdemServicoService ordemServicoService;

    @Test
    @DisplayName("UC05 - Teste 01: POST /api/veiculos cadastra veículo válido e retorna 201 Created")
    void deveCadastrarVeiculoComSucesso() throws Exception {
        VeiculoRequest request = new VeiculoRequest(
                1,
                "XYZ-9876",
                "Ford",
                "Ka",
                2019,
                35000.0
        );

        VeiculoResponse response = new VeiculoResponse(
                1,
                "XYZ-9876",
                "Ka",
                2019,
                "Ford",
                35000.0,
                true,
                1,
                "João da Silva",
                "12345678901",
                "Veículo cadastrado com sucesso"
        );

        when(veiculoService.cadastrar(any(VeiculoRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/veiculos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/veiculos/1"))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.placa").value("XYZ-9876"))
                .andExpect(jsonPath("$.montadora").value("Ford"))
                .andExpect(jsonPath("$.modelo").value("Ka"))
                .andExpect(jsonPath("$.ano").value(2019))
                .andExpect(jsonPath("$.valorFipe").value(35000.0))
                .andExpect(jsonPath("$.clienteNome").value("João da Silva"))
                .andExpect(jsonPath("$.mensagem").value("Veículo cadastrado com sucesso"));
    }

    @Test
    @DisplayName("UC05 - Teste 02: POST /api/veiculos com placa duplicada retorna 409 Conflict")
    void deveRetornar409ParaPlacaDuplicada() throws Exception {
        VeiculoRequest request = new VeiculoRequest(
                1,
                "XYZ-9876",
                "Ford",
                "Ka",
                2019,
                35000.0
        );

        when(veiculoService.cadastrar(any(VeiculoRequest.class)))
                .thenThrow(new ConflitoException("Já existe um veículo cadastrado com esta placa no sistema."));

        mockMvc.perform(post("/api/veiculos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message").value("Já existe um veículo cadastrado com esta placa no sistema."));
    }

    @Test
    @DisplayName("UC05 - Teste 03: PUT /api/veiculos/{id} atualiza dados do veículo e retorna 200 OK")
    void deveAtualizarDadosDoVeiculo() throws Exception {
        VeiculoAtualizacaoRequest request = new VeiculoAtualizacaoRequest(
                null,
                null,
                null,
                33000.0,
                null,
                null,
                null
        );

        VeiculoResponse response = new VeiculoResponse(
                1,
                "XYZ-9876",
                "Ka",
                2019,
                "Ford",
                33000.0,
                true,
                1,
                "João da Silva",
                "12345678901",
                "Dados atualizados com sucesso"
        );

        when(veiculoService.atualizar(eq(1), any(VeiculoAtualizacaoRequest.class))).thenReturn(response);

        mockMvc.perform(put("/api/veiculos/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valorFipe").value(33000.0))
                .andExpect(jsonPath("$.mensagem").value("Dados atualizados com sucesso"));
    }

    @Test
    @DisplayName("UC05 - Teste 04: POST /api/veiculos com placa inválida retorna 400 Bad Request")
    void deveRetornar400ParaPlacaInvalida() throws Exception {
        VeiculoRequest request = new VeiculoRequest(
                1,
                "ABC-12",
                "Ford",
                "Ka",
                2019,
                35000.0
        );

        mockMvc.perform(post("/api/veiculos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Formato de placa inválido. Utilize o padrão AAA-1234 ou Mercosul."));
    }

    @Test
    @DisplayName("UC05: PATCH /api/veiculos/{id}/inativar inativa veículo com 204 No Content")
    void deveInativarVeiculo() throws Exception {
        doNothing().when(veiculoService).inativar(1);

        mockMvc.perform(patch("/api/veiculos/1/inativar"))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("UC06 - Teste 01: GET /api/veiculos/{placa}/historico retorna ordens da mais recente para a mais antiga")
    void deveConsultarHistoricoComSucesso() throws Exception {
        VeiculoResumo veiculoResumo = new VeiculoResumo(1, "XYZ-9876", "Ka", "Ford", 2019, 35000.0, true, "João da Silva");
        OrdemServicoHistoricoResponse ordem = new OrdemServicoHistoricoResponse(
                10,
                LocalDate.of(2026, 10, 1),
                LocalDate.of(2026, 10, 2),
                LocalDate.of(2026, 10, 3),
                null,
                StatusServico.FINALIZADO,
                "Revisão de 50.000 km",
                "Carlos Mecânico",
                "Oficina Central",
                List.of(),
                List.of(),
                BigDecimal.valueOf(350.0)
        );

        HistoricoVeiculoResponse historico = new HistoricoVeiculoResponse(
                veiculoResumo,
                "Histórico recuperado com sucesso.",
                List.of(ordem),
                1
        );

        when(ordemServicoService.consultarHistoricoPorPlaca("XYZ-9876")).thenReturn(historico);

        mockMvc.perform(get("/api/veiculos/XYZ-9876/historico"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.veiculo.placa").value("XYZ-9876"))
                .andExpect(jsonPath("$.mensagem").value("Histórico recuperado com sucesso."))
                .andExpect(jsonPath("$.totalOrdens").value(1))
                .andExpect(jsonPath("$.ordensServico[0].id").value(10))
                .andExpect(jsonPath("$.ordensServico[0].valorTotal").value(350.0));
    }

    @Test
    @DisplayName("UC06 - Teste 02: GET /api/veiculos/{placa}/historico para veículo sem ordens")
    void deveConsultarHistoricoVeiculoSemOrdens() throws Exception {
        VeiculoResumo veiculoResumo = new VeiculoResumo(1, "XYZ-9876", "Ka", "Ford", 2019, 35000.0, true, "João da Silva");
        HistoricoVeiculoResponse historico = new HistoricoVeiculoResponse(
                veiculoResumo,
                "Nenhuma ordem de serviço encontrada para este veículo.",
                List.of(),
                0
        );

        when(ordemServicoService.consultarHistoricoPorPlaca("XYZ-9876")).thenReturn(historico);

        mockMvc.perform(get("/api/veiculos/XYZ-9876/historico"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.veiculo.placa").value("XYZ-9876"))
                .andExpect(jsonPath("$.mensagem").value("Nenhuma ordem de serviço encontrada para este veículo."))
                .andExpect(jsonPath("$.ordensServico").isEmpty());
    }

    @Test
    @DisplayName("UC06 - Teste 03: GET /api/veiculos/{placa}/historico para placa inexistente retorna 404")
    void deveRetornar404ParaPlacaInexistenteNoHistorico() throws Exception {
        when(ordemServicoService.consultarHistoricoPorPlaca("NAO-0000"))
                .thenThrow(new RecursoNaoEncontradoException("Veículo não encontrado."));

        mockMvc.perform(get("/api/veiculos/NAO-0000/historico"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Veículo não encontrado."));
    }
}

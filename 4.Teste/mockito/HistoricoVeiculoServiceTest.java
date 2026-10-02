package com.cartoon.api.ordemServico;

import com.cartoon.api.cliente.Cliente;
import com.cartoon.api.compartilhado.exceptions.RecursoNaoEncontradoException;
import com.cartoon.api.oficina.Oficina;
import com.cartoon.api.ordemServico.dto.response.ItemPecaResponse;
import com.cartoon.api.ordemServico.dto.response.ItemServicoResponse;
import com.cartoon.api.ordemServico.dto.response.OrdemServicoHistoricoResponse;
import com.cartoon.api.ordemServico.models.ItemPeca;
import com.cartoon.api.ordemServico.models.ItemServico;
import com.cartoon.api.ordemServico.models.OrdemServico;
import com.cartoon.api.ordemServico.models.StatusServico;
import com.cartoon.api.ordemServico.repositories.OrdemServicoRepository;
import com.cartoon.api.ordemServico.service.OrdemServicoService;
import com.cartoon.api.peca.Peca;
import com.cartoon.api.servico.Servico;
import com.cartoon.api.usuario.Role;
import com.cartoon.api.usuario.Usuario;
import com.cartoon.api.veiculo.Veiculo;
import com.cartoon.api.veiculo.VeiculoService;
import com.cartoon.api.veiculo.dto.response.HistoricoVeiculoResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

/**
 * Testes Unitários com Mockito para UC06 - Consultar Histórico do Veículo
 * Rastreabilidade: CART-US07-Consultar-Historico-do-Veiculo.docx
 */
@ExtendWith(MockitoExtension.class)
public class HistoricoVeiculoServiceTest {

    @Mock
    private OrdemServicoRepository ordemServicoRepository;

    @Mock
    private VeiculoService veiculoService;

    @InjectMocks
    private OrdemServicoService ordemServicoService;

    private Veiculo veiculo;
    private Oficina oficina;
    private Usuario mecanico;
    private OrdemServico ordemRecente;
    private OrdemServico ordemAntiga;

    @BeforeEach
    void setUp() {
        Cliente cliente = new Cliente();
        cliente.setId(1);
        cliente.setNome("João da Silva");
        cliente.setCpf("12345678901");

        veiculo = new Veiculo();
        veiculo.setId(1);
        veiculo.setPlaca("XYZ-9876");
        veiculo.setModelo("Ka");
        veiculo.setMontadora("Ford");
        veiculo.setAno(2019);
        veiculo.setValorFipe(35000.0);
        veiculo.setAtivo(true);
        veiculo.setCliente(cliente);

        oficina = new Oficina();
        oficina.setId(1);
        oficina.setNome("Oficina Central");

        mecanico = new Usuario();
        mecanico.setId(1);
        mecanico.setNome("Carlos Mecânico");
        mecanico.setRole(Role.MECANICO);

        Servico servico = new Servico();
        servico.setId(1);
        servico.setNome("Troca de Óleo");
        servico.setValorBase(100.0);

        Peca peca = new Peca();
        peca.setId(1);
        peca.setNome("Filtro de Óleo");
        peca.setFabricante("Bosch");
        peca.setValorBase(BigDecimal.valueOf(50.0));

        // Ordem Recente (01/10/2026)
        ordemRecente = new OrdemServico();
        ordemRecente.setId(10);
        ordemRecente.setDescricao("Revisão de 50.000 km");
        ordemRecente.setDataOrcamento(LocalDate.of(2026, 10, 1));
        ordemRecente.setDataInicio(LocalDate.of(2026, 10, 2));
        ordemRecente.setDataFinalizacao(LocalDate.of(2026, 10, 3));
        ordemRecente.setStatusServico(StatusServico.FINALIZADO);
        ordemRecente.setVeiculo(veiculo);
        ordemRecente.setOficina(oficina);
        ordemRecente.setMecanico(mecanico);
        ordemRecente.setTotal(BigDecimal.valueOf(350.0));

        ItemServico itemServico = new ItemServico();
        itemServico.setId(1);
        itemServico.setServico(servico);
        itemServico.setQuantidade(1);
        itemServico.setValorUnitario(100.0);
        itemServico.setSubtotal(100.0);
        itemServico.setTempo(LocalTime.of(1, 0));
        ordemRecente.adicionarItemServico(itemServico);

        ItemPeca itemPeca = new ItemPeca();
        itemPeca.setId(1);
        itemPeca.setPeca(peca);
        itemPeca.setQuantidade(1);
        itemPeca.setValorUnitario(BigDecimal.valueOf(50.0));
        itemPeca.setSubtotal(BigDecimal.valueOf(50.0));
        ordemRecente.adicionarItemPeca(itemPeca);

        // Ordem Antiga (15/05/2026)
        ordemAntiga = new OrdemServico();
        ordemAntiga.setId(5);
        ordemAntiga.setDescricao("Alinhamento e Balanceamento");
        ordemAntiga.setDataOrcamento(LocalDate.of(2026, 5, 15));
        ordemAntiga.setStatusServico(StatusServico.FINALIZADO);
        ordemAntiga.setVeiculo(veiculo);
        ordemAntiga.setOficina(oficina);
        ordemAntiga.setMecanico(mecanico);
        ordemAntiga.setTotal(BigDecimal.valueOf(120.0));
    }

    @Test
    @DisplayName("UC06 - Caso 01: Consulta por placa existente com ordens da mais recente para a mais antiga")
    void deveConsultarHistoricoPorPlacaExistenteComOrdens() {
        when(veiculoService.buscarEntidadePorPlaca("XYZ-9876")).thenReturn(veiculo);
        when(ordemServicoRepository.findByVeiculoPlacaIgnoreCaseOrderByDataOrcamentoDescIdDesc("XYZ-9876"))
                .thenReturn(List.of(ordemRecente, ordemAntiga));

        HistoricoVeiculoResponse response = ordemServicoService.consultarHistoricoPorPlaca("XYZ-9876");

        assertThat(response).isNotNull();
        assertThat(response.veiculo().placa()).isEqualTo("XYZ-9876");
        assertThat(response.mensagem()).isEqualTo("Histórico recuperado com sucesso.");
        assertThat(response.totalOrdens()).isEqualTo(2);

        List<OrdemServicoHistoricoResponse> ordens = response.ordensServico();
        assertThat(ordens).hasSize(2);

        // Ordem mais recente em primeiro lugar
        OrdemServicoHistoricoResponse primeira = ordens.get(0);
        assertThat(primeira.id()).isEqualTo(10);
        assertThat(primeira.dataOrcamento()).isEqualTo(LocalDate.of(2026, 10, 1));
        assertThat(primeira.status()).isEqualTo(StatusServico.FINALIZADO);
        assertThat(primeira.mecanicoNome()).isEqualTo("Carlos Mecânico");
        assertThat(primeira.valorTotal()).isEqualByComparingTo(BigDecimal.valueOf(350.0));
        assertThat(primeira.itensServico()).hasSize(1);
        assertThat(primeira.itensPeca()).hasSize(1);

        // Ordem mais antiga em segundo lugar
        OrdemServicoHistoricoResponse segunda = ordens.get(1);
        assertThat(segunda.id()).isEqualTo(5);
        assertThat(segunda.dataOrcamento()).isEqualTo(LocalDate.of(2026, 5, 15));
    }

    @Test
    @DisplayName("UC06 - Caso 02: Consulta de veículo sem histórico exibe mensagem amigável")
    void deveRetornarMensagemQuandoVeiculoNaoPossuiOrdens() {
        when(veiculoService.buscarEntidadePorPlaca("XYZ-9876")).thenReturn(veiculo);
        when(ordemServicoRepository.findByVeiculoPlacaIgnoreCaseOrderByDataOrcamentoDescIdDesc("XYZ-9876"))
                .thenReturn(List.of());

        HistoricoVeiculoResponse response = ordemServicoService.consultarHistoricoPorPlaca("XYZ-9876");

        assertThat(response).isNotNull();
        assertThat(response.veiculo().placa()).isEqualTo("XYZ-9876");
        assertThat(response.mensagem()).isEqualTo("Nenhuma ordem de serviço encontrada para este veículo.");
        assertThat(response.ordensServico()).isEmpty();
        assertThat(response.totalOrdens()).isEqualTo(0);
    }

    @Test
    @DisplayName("UC06 - Caso 03: Consulta por placa inexistente retorna erro 'Veículo não encontrado.'")
    void deveLancarExcecaoQuandoPlacaNaoCadastrada() {
        when(veiculoService.buscarEntidadePorPlaca("NAO-9999"))
                .thenThrow(new RecursoNaoEncontradoException("Veículo não encontrado."));

        assertThatThrownBy(() -> ordemServicoService.consultarHistoricoPorPlaca("NAO-9999"))
                .isInstanceOf(RecursoNaoEncontradoException.class)
                .hasMessage("Veículo não encontrado.");

        verify(ordemServicoRepository, never()).findByVeiculoPlacaIgnoreCaseOrderByDataOrcamentoDescIdDesc(anyString());
    }

    @Test
    @DisplayName("UC06 - Caso 04: Detalhar uma ordem do histórico exibindo itens de serviço, itens de peça, mecânico e status")
    void deveDetalharOrdemDoHistorico() {
        when(ordemServicoRepository.findDetalhadaById(10)).thenReturn(Optional.of(ordemRecente));

        OrdemServicoHistoricoResponse detalhe = ordemServicoService.detalharOrdem(10);

        assertThat(detalhe).isNotNull();
        assertThat(detalhe.id()).isEqualTo(10);
        assertThat(detalhe.mecanicoNome()).isEqualTo("Carlos Mecânico");
        assertThat(detalhe.status()).isEqualTo(StatusServico.FINALIZADO);
        assertThat(detalhe.descricao()).isEqualTo("Revisão de 50.000 km");

        assertThat(detalhe.itensServico()).hasSize(1);
        ItemServicoResponse servicoItem = detalhe.itensServico().get(0);
        assertThat(servicoItem.servicoNome()).isEqualTo("Troca de Óleo");
        assertThat(servicoItem.subtotal()).isEqualTo(100.0);

        assertThat(detalhe.itensPeca()).hasSize(1);
        ItemPecaResponse pecaItem = detalhe.itensPeca().get(0);
        assertThat(pecaItem.pecaNome()).isEqualTo("Filtro de Óleo");
        assertThat(pecaItem.subtotal()).isEqualByComparingTo(BigDecimal.valueOf(50.0));
    }

    @Test
    @DisplayName("UC06 - Caso 05: Listagem paginada do histórico veicular")
    void deveConsultarHistoricoPaginado() {
        Pageable pageable = PageRequest.of(0, 1);
        Page<OrdemServico> page = new PageImpl<>(List.of(ordemRecente), pageable, 2);

        when(veiculoService.buscarEntidadePorPlaca("XYZ-9876")).thenReturn(veiculo);
        when(ordemServicoRepository.findByVeiculoPlacaIgnoreCaseOrderByDataOrcamentoDescIdDesc("XYZ-9876", pageable))
                .thenReturn(page);

        Page<OrdemServicoHistoricoResponse> resultado =
                ordemServicoService.consultarHistoricoPaginadoPorPlaca("XYZ-9876", pageable);

        assertThat(resultado).isNotNull();
        assertThat(resultado.getTotalElements()).isEqualTo(2);
        assertThat(resultado.getContent()).hasSize(1);
        assertThat(resultado.getContent().get(0).id()).isEqualTo(10);
    }
}

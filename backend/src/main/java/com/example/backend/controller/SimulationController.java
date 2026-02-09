package com.example.backend.controller;
import org.springframework.web.bind.annotation.*;
import com.example.backend.service.SimulationService;
import com.example.backend.dto.SimulationRequest;
import com.example.backend.dto.SimulationResponse;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1")
public class SimulationController {
    //フィールド設定
    private final SimulationService simulationService;

    //コンストラクタ
    public SimulationController(SimulationService simulationService) {
        this.simulationService = simulationService;
    }

    @PostMapping("/simulations")
    public SimulationResponse run(@Valid @RequestBody SimulationRequest req) {
        return simulationService.run(req);
    }
}

package com.gamja.tiggle.program.adapter.out.persistence;

import com.gamja.tiggle.common.BaseException;
import com.gamja.tiggle.common.BaseResponseStatus;

import com.gamja.tiggle.program.application.port.out.ProgramPort;
import com.gamja.tiggle.program.application.port.out.ReadProgramPort;
import lombok.RequiredArgsConstructor;
import com.gamja.tiggle.common.annotation.PersistenceAdapter;
import com.gamja.tiggle.program.application.port.out.CreateProgramPort;
import com.gamja.tiggle.program.domain.Program;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@PersistenceAdapter
@RequiredArgsConstructor
public class ProgramPersistenceAdapter implements CreateProgramPort, ReadProgramPort, ProgramPort {
    private final JpaProgramRepository jpaProgramRepository;
    private final JpaProgramImageRepository jpaProgramImageRepository;
    private final JpaCategoryRepository jpaCategoryRepository;

    @Override
    public void createProgram(Program program) throws BaseException {

        CategoryEntity result = jpaCategoryRepository.findById(program.getCategoryId())
                .orElseThrow(() -> new BaseException(BaseResponseStatus.FAIL));

        ProgramEntity entity = ProgramEntity.builder()
                .categoryEntity(result)
                .reservationOpenDate(program.getReservationOpenDate())
                .programName(program.getProgramName())
                .programInfo(program.getProgramInfo())
                .programStartDate(program.getProgramStartDate())
                .programEndDate(program.getProgramEndDate())
                .build();
        jpaProgramRepository.save(entity);


        for (String uploadFilePath : program.getImageUrls()) {
            ProgramImageEntity programImageEntity = ProgramImageEntity.builder()
                    .imgUrl(uploadFilePath)
                    .programEntity(entity)
                    .build();
            jpaProgramImageRepository.save(programImageEntity);
        }

    }

    @Override
    public List<Program> readProgramAll(Program program) throws BaseException {

        List<ProgramEntity> result = jpaProgramRepository.findAllByCategoryEntity(program.getCategoryId());
        List<Program> programs = result.stream().map(
                (p) -> Program.builder()
                        .categoryId(p.getCategoryEntity().getId())
                        .programName(p.getProgramName())
                        .programInfo(p.getProgramInfo())
                        .programStartDate(p.getProgramStartDate())
                        .programEndDate(p.getProgramEndDate())
                        .imageUrls(p.getProgramImageEntities().stream().map(programImageEntity -> programImageEntity.getImgUrl()).toList())
                        .build()
        ).collect(Collectors.toUnmodifiableList());

        return programs;
    }

    @Override
    public List<Program> readRealTimeAll(LocalDateTime currentDateTime) throws BaseException {
        List<ProgramEntity> programEntities = jpaProgramRepository.findAll();
        List<Program> programs = programEntities.stream()
                // p의 값이 현재 시간보다 after 인 경우
                .filter(p -> p.getReservationOpenDate().isAfter(currentDateTime))
                // 오름차순 반환 : 리스트 내에 있는 값 두 개씩 비교해서 출력
                .sorted((p1, p2) -> p1.getReservationOpenDate().compareTo(p2.getReservationOpenDate()))
                .map(p -> Program.builder()
                        .categoryId(p.getCategoryEntity().getId())
                        .programName(p.getProgramName())
                        .programInfo(p.getProgramInfo())
                        .programStartDate(p.getProgramStartDate())
                        .programEndDate(p.getProgramEndDate())
                        .reservationOpenDate(p.getReservationOpenDate())
                        .imageUrls(p.getProgramImageEntities().stream().map(programImageEntity -> programImageEntity.getImgUrl()).collect(Collectors.toList()))
                        .build())
                .collect(Collectors.toUnmodifiableList());

        return programs;
    }

    @Override
    public boolean existProgram(Long id) {
        return jpaProgramRepository.existsById(id);

    }
}

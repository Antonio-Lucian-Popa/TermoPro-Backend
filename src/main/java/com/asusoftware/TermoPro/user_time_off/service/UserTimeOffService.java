package com.asusoftware.TermoPro.user_time_off.service;

import com.asusoftware.TermoPro.exception.ResourceNotFoundException;
import com.asusoftware.TermoPro.user.repository.UserRepository;
import com.asusoftware.TermoPro.user_time_off.repository.UserTimeOffRepository;
import com.asusoftware.TermoPro.user_time_off.model.TimeOffType;
import com.asusoftware.TermoPro.user_time_off.model.UserTimeOff;
import com.asusoftware.TermoPro.user_time_off.model.dto.CreateTimeOffDto;
import com.asusoftware.TermoPro.user_time_off.model.dto.TimeOffDto;
import lombok.RequiredArgsConstructor;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserTimeOffService {

    private final UserTimeOffRepository repository;
    private final UserRepository userRepository;
    private final ModelMapper mapper;

    @Transactional
    public TimeOffDto requestTimeOff(CreateTimeOffDto dto) {
        if (!userRepository.existsById(dto.getUserId())) {
            throw new ResourceNotFoundException("Utilizatorul nu există.");
        }

        if (dto.getStartDate().isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Nu poți solicita timp liber în trecut.");
        }

        if (dto.getEndDate().isBefore(dto.getStartDate())) {
            throw new IllegalArgumentException("Data de sfârșit nu poate fi înainte de data de început.");
        }

        if (dto.getType() == TimeOffType.INVOIRE &&
                (dto.getStartTime() == null || dto.getEndTime() == null)) {
            throw new IllegalArgumentException("Pentru invoire, trebuie să specifici ora de început și sfârșit.");
        }

        UserTimeOff timeOff = UserTimeOff.builder()
                .userId(dto.getUserId())
                .startDate(dto.getStartDate())
                .endDate(dto.getEndDate())
                .type(dto.getType())
                .startTime(dto.getStartTime())
                .endTime(dto.getEndTime())
                .approved(false)
                .build();

        repository.save(timeOff);
        return mapper.map(timeOff, TimeOffDto.class);
    }

    public List<TimeOffDto> getTimeOffsForUser(UUID userId) {
        return repository.findAllByUserId(userId).stream()
                .map(r -> mapper.map(r, TimeOffDto.class))
                .toList();
    }

    public List<TimeOffDto> getAllTimeOffsByDate(LocalDate date) {
        return repository.findAllByStartDateLessThanEqualAndEndDateGreaterThanEqual(date, date).stream()
                .map(r -> mapper.map(r, TimeOffDto.class))
                .toList();
    }

    public List<TimeOffDto> getPendingRequestsForCompany(UUID companyId) {
        return repository.findAllPendingByCompany(companyId).stream()
                .map(r -> mapper.map(r, TimeOffDto.class))
                .toList();
    }

    @Transactional
    public void approveRequest(UUID requestId) {
        UserTimeOff request = repository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Cererea nu există."));
        request.setApproved(true);
        repository.save(request);
    }

    @Transactional
    public void rejectRequest(UUID requestId) {
        UserTimeOff request = repository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Cererea nu există."));
        repository.delete(request);
    }

    public byte[] exportTimeOffsToExcel(UUID userId) {
        List<UserTimeOff> list = repository.findAllByUserId(userId);
        if (list.isEmpty()) throw new ResourceNotFoundException("Nu există cereri.");

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Cereri Time Off");

            Row header = sheet.createRow(0);
            header.createCell(0).setCellValue("Start");
            header.createCell(1).setCellValue("Sfârșit");
            header.createCell(2).setCellValue("Tip");
            header.createCell(3).setCellValue("Start Ora");
            header.createCell(4).setCellValue("End Ora");
            header.createCell(5).setCellValue("Aprobat");

            int rowIdx = 1;
            for (UserTimeOff off : list) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(off.getStartDate().toString());
                row.createCell(1).setCellValue(off.getEndDate().toString());
                row.createCell(2).setCellValue(off.getType().name());
                row.createCell(3).setCellValue(off.getStartTime() != null ? off.getStartTime().toString() : "-");
                row.createCell(4).setCellValue(off.getEndTime() != null ? off.getEndTime().toString() : "-");
                row.createCell(5).setCellValue(Boolean.TRUE.equals(off.getApproved()) ? "DA" : "NU");
            }

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write(out);
            return out.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Eroare la generarea fișierului Excel", e);
        }
    }

    public byte[] exportTimeOffsToPdf(UUID userId) {
        List<UserTimeOff> requests = repository.findAllByUserId(userId);
        if (requests.isEmpty()) throw new ResourceNotFoundException("Nu există cereri.");

        try (PDDocument doc = new PDDocument()) {
            PDPage page = new PDPage(PDRectangle.A4);
            doc.addPage(page);

            PDPageContentStream content = new PDPageContentStream(doc, page);
            content.setFont(PDType1Font.HELVETICA_BOLD, 14);
            content.beginText();
            content.newLineAtOffset(50, 770);
            content.showText("Cereri timp liber - userId: " + userId);
            content.endText();

            float y = 740;
            float margin = 50;
            float leading = 18f;
            content.setFont(PDType1Font.HELVETICA, 11);

            for (UserTimeOff off : requests) {
                if (y < 70) {
                    content.close();
                    page = new PDPage(PDRectangle.A4);
                    doc.addPage(page);
                    content = new PDPageContentStream(doc, page);
                    y = 770;
                }

                content.beginText();
                content.newLineAtOffset(margin, y);
                content.showText(
                        "- " + off.getStartDate() + " → " + off.getEndDate()
                                + " | " + off.getType()
                                + (off.getStartTime() != null ? " " + off.getStartTime() : "")
                                + (off.getEndTime() != null ? " - " + off.getEndTime() : "")
                                + " | Aprobat: " + (Boolean.TRUE.equals(off.getApproved()) ? "DA" : "NU")
                );
                content.endText();
                y -= leading;
            }

            content.close();
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            doc.save(out);
            return out.toByteArray();

        } catch (IOException e) {
            throw new RuntimeException("Eroare la generarea PDF-ului", e);
        }
    }
}

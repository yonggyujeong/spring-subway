package subway.domain.application;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import subway.domain.Line;
import subway.domain.Section;
import subway.domain.Station;
import subway.domain.SubwayGraph;
import subway.domain.dto.AddSectionDto;
import subway.domain.dto.LineDto;
import subway.domain.dto.SubwayPathDto;
import subway.domain.repository.LineRepository;
import subway.domain.repository.SectionRepository;
import subway.domain.repository.StationRepository;
import subway.domain.searchGraph.SearchGraph;
import subway.domain.vo.Distance;
import subway.domain.vo.SubwayPath;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class SubwayGraphService {
    private final SubwayGraph subwayGraph;
    private final SectionRepository sectionRepository;
    private final LineRepository lineRepository;
    private final StationRepository stationRepository;
    private final SearchGraph searchGraph;

    public SubwayGraphService(SubwayGraph subwayGraph, SectionRepository sectionRepository,
                              LineRepository lineRepository, StationRepository stationRepository,
                              SearchGraph searchGraph) {
        this.subwayGraph = subwayGraph;
        this.sectionRepository = sectionRepository;
        this.lineRepository = lineRepository;
        this.stationRepository = stationRepository;
        this.searchGraph = searchGraph;
    }

    /**
     * DB의 구간 데이터를 조회하여 지하철그래프와 탐색그래프의 상태를 초기화한다.
     */
    public void initGraph() {
        List<Station> allStation = stationRepository.findAll();
        for (Station station : allStation) {
            searchGraph.addStation(station);
        }

        List<Section> allSection = sectionRepository.findAll();
        for (Section section : allSection) {
            subwayGraph.add(section);
            searchGraph.addSection(section);
        }

    }

    /**
     * 지하철 그래프, 탐색 그래프, DB에 구간을 추가합니다.
     * @param addSectionDto
     * @return
     */
    @Transactional
    public Section addSection(AddSectionDto addSectionDto) {
        Line line = lineRepository.findById(addSectionDto.getLineId());
        Station upStation = stationRepository.findById(addSectionDto.getUpStationId());
        Station downStation = stationRepository.findById(addSectionDto.getDownStationId());
        Integer distance = addSectionDto.getDistance();
        Section section = Section.of(line, upStation, downStation, distance);

        subwayGraph.add(section);
        searchGraph.addSection(section);
        return sectionRepository.insert(section);
    }

    /**
     * 그래프와 DB에서 해당 호선의 구간(역)을 제거합니다.
     * @param lineId
     * @param stationId
     */
    @Transactional
    public void removeStation(Long lineId, Long stationId) {
        Line line = lineRepository.findById(lineId);
        Station station = stationRepository.findById(stationId);
        Section removedSection = subwayGraph.remove(line, station);
        try {
            sectionRepository.deleteById(removedSection.getId());
        } catch (Exception e) {
            subwayGraph.add(removedSection);        // 롤백
            log.error(e.getMessage());
            throw e;
        }
    }

    /**
     * 호선 번호에 해당하는 호선정보와 역 목록을 조회합니다.
     * @param lineId
     * @return
     */
    public LineDto getLineWithStations(Long lineId) {
        Line line = lineRepository.findById(lineId);
        return LineDto.from(subwayGraph.getLineWithStations(line));
    }

    /**
     * 전체 호선 정보와 역 목록을 조회합니다.
     * @return
     */
    public List<LineDto> getAllLineWithStations() {
        List<Line> lines = lineRepository.findAll();
        return lines.stream()
                .map(line -> subwayGraph.getLineWithStations(line))
                .map(LineDto::from)
                .collect(Collectors.toList());
    }

    /**
     * 최단거리 경로를 찾는다.
     * @param startStationId
     * @param endStationId
     * @return
     */
    public SubwayPathDto findShortenPath(Long startStationId, Long endStationId) {
        Station startStation = stationRepository.findById(startStationId);
        Station endStation = stationRepository.findById(endStationId);
        return SubwayPathDto.from(searchGraph.findShortenPath(startStation, endStation));
    }

}

package com.onion.location;

import java.io.IOException;
import java.util.List;

import com.onion.domain.Location;
import com.onion.exception.LocationNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class LocationController {
    @Autowired
    private LocationService service;

    // 지역 목록 첫번째 페이지 GET
    @GetMapping("/locations")
    public String listFirstPage(String sortDir, Model model) {
        return listByPage(1, sortDir, null, model);
    }

    // 지역 목록 페이징 GET
    // default 오름차순
    @GetMapping("/locations/page/{pageNum}")
    public String listByPage(@PathVariable(name = "pageNum") int pageNum,
                             String sortDir, String keyword, Model model) {
        if (sortDir == null || sortDir.isEmpty()) {
            sortDir = "asc";
        }

        LocationPageInfo pageInfo = new LocationPageInfo();
        List<Location> listLocations = service.listByPage(pageInfo, pageNum, sortDir, keyword);

        long startCount = (pageNum - 1) * LocationService.ROOT_LOCATIONS_PER_PAGE + 1;
        long endCount = startCount + LocationService.ROOT_LOCATIONS_PER_PAGE - 1;
        if (endCount > pageInfo.getTotalElements()) {
            endCount = pageInfo.getTotalElements();
        }

        String reverseSortDir = sortDir.equals("asc") ? "desc" : "asc";

        model.addAttribute("totalPages", pageInfo.getTotalPages());
        model.addAttribute("totalItems", pageInfo.getTotalElements());
        model.addAttribute("currentPage", pageNum);
        model.addAttribute("sortField", "name");
        model.addAttribute("sortDir", sortDir);
        model.addAttribute("keyword", keyword);
        model.addAttribute("startCount", startCount);
        model.addAttribute("endCount", endCount);

        model.addAttribute("listLocations", listLocations);
        model.addAttribute("reverseSortDir", reverseSortDir);
        model.addAttribute("moduleURL", "/locations");

        return "locations/locations";
    }

    // 지역 생성 폼 GET
    @GetMapping("/locations/new")
    public String newLocation(Model model) {
        List<Location> listLocations = service.listLocationsUsedInForm();

        model.addAttribute("location", new Location());
        model.addAttribute("listLocations", listLocations);
        model.addAttribute("pageTitle", "지역 추가");

        return "locations/location_form";
    }

    // 지역 저장
    @PostMapping("/locations/save")
    public String saveLocation(Location location, RedirectAttributes ra) throws IOException {
        service.save(location);
        ra.addFlashAttribute("message", "지역가 성공적으로 저장되었습니다.");
        return "redirect:/locations";
    }

    // 지역 수정 폼 GET
    @GetMapping("/locations/edit/{id}")
    public String editLocation(@PathVariable(name = "id") Integer id, Model model,
                               RedirectAttributes ra) {
        try {
            Location location = service.get(id);
            List<Location> listLocations = service.listLocationsUsedInForm();

            model.addAttribute("location", location);
            model.addAttribute("listLocations", listLocations);
            model.addAttribute("pageTitle", "지역 수정 (ID: " + id + ")");

            return "locations/location_form";
        } catch (LocationNotFoundException ex) {
            ra.addFlashAttribute("message", ex.getMessage());
            return "redirect:/locations";
        }
    }

    // 지역 활성화 GET
    @GetMapping("/locations/{id}/enabled/{status}")
    public String updateLocationEnabledStatus(@PathVariable("id") Integer id,
                                              @PathVariable("status") boolean enabled, RedirectAttributes redirectAttributes) {
        service.updateLocationEnabledStatus(id, enabled);
        String status = enabled ? "enabled" : "disabled";
        String message = "지역 ID : " + id + "를 " + status + "했습니다.";
        redirectAttributes.addFlashAttribute("message", message);

        return "redirect:/locations";
    }

    // 지역 삭제 GET
    @GetMapping("/locations/delete/{id}")
    public String deleteLocation(@PathVariable(name = "id") Integer id,
                                 Model model,
                                 RedirectAttributes redirectAttributes) {
        try {
            service.delete(id);
            redirectAttributes.addFlashAttribute("message",
                    "지역 ID " + id + " 가 삭제되었습니다.");
        } catch (LocationNotFoundException ex) {
            redirectAttributes.addFlashAttribute("message", ex.getMessage());
        }

        return "redirect:/locations";
    }

}

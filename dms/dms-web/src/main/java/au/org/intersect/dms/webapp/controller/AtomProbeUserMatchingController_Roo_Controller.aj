// WARNING: DO NOT EDIT THIS FILE. THIS FILE IS MANAGED BY SPRING ROO.
// You may push code into the target .java compilation unit if you wish to edit any member(s).

package au.org.intersect.dms.webapp.controller;

import au.org.intersect.dms.webapp.domain.AtomProbeUserMatching;
import au.org.intersect.dms.webapp.domain.StockServer;
import java.io.UnsupportedEncodingException;
import java.lang.Long;
import java.lang.String;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.convert.support.GenericConversionService;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.util.UriUtils;
import org.springframework.web.util.WebUtils;

privileged aspect AtomProbeUserMatchingController_Roo_Controller {
    
    @Autowired
    private GenericConversionService AtomProbeUserMatchingController.conversionService;
    
    @RequestMapping(method = RequestMethod.POST)
    public String AtomProbeUserMatchingController.create(@Valid AtomProbeUserMatching atomProbeUserMatching, BindingResult result, Model model, HttpServletRequest request) {
        if (result.hasErrors()) {
            model.addAttribute("atomProbeUserMatching", atomProbeUserMatching);
            return "admin/atomprobeusermatchings/create";
        }
        atomProbeUserMatching.persist();
        return "redirect:/admin/atomprobeusermatchings/" + encodeUrlPathSegment(atomProbeUserMatching.getId().toString(), request);
    }
    
    @RequestMapping(params = "form", method = RequestMethod.GET)
    public String AtomProbeUserMatchingController.createForm(Model model) {
        model.addAttribute("atomProbeUserMatching", new AtomProbeUserMatching());
        List dependencies = new ArrayList();
        if (StockServer.countStockServers() == 0) {
            dependencies.add(new String[]{"instrument", "stockservers"});
        }
        model.addAttribute("dependencies", dependencies);
        return "admin/atomprobeusermatchings/create";
    }
    
    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public String AtomProbeUserMatchingController.show(@PathVariable("id") Long id, Model model) {
        model.addAttribute("atomprobeusermatching", AtomProbeUserMatching.findAtomProbeUserMatching(id));
        model.addAttribute("itemId", id);
        return "admin/atomprobeusermatchings/show";
    }
    
    @RequestMapping(method = RequestMethod.GET)
    public String AtomProbeUserMatchingController.list(@RequestParam(value = "page", required = false) Integer page, @RequestParam(value = "size", required = false) Integer size, Model model) {
        if (page != null || size != null) {
            int sizeNo = size == null ? 10 : size.intValue();
            model.addAttribute("atomprobeusermatchings", AtomProbeUserMatching.findAtomProbeUserMatchingEntries(page == null ? 0 : (page.intValue() - 1) * sizeNo, sizeNo));
            float nrOfPages = (float) AtomProbeUserMatching.countAtomProbeUserMatchings() / sizeNo;
            model.addAttribute("maxPages", (int) ((nrOfPages > (int) nrOfPages || nrOfPages == 0.0) ? nrOfPages + 1 : nrOfPages));
        } else {
            model.addAttribute("atomprobeusermatchings", AtomProbeUserMatching.findAllAtomProbeUserMatchings());
        }
        return "admin/atomprobeusermatchings/list";
    }
    
    @RequestMapping(method = RequestMethod.PUT)
    public String AtomProbeUserMatchingController.update(@Valid AtomProbeUserMatching atomProbeUserMatching, BindingResult result, Model model, HttpServletRequest request) {
        if (result.hasErrors()) {
            model.addAttribute("atomProbeUserMatching", atomProbeUserMatching);
            return "admin/atomprobeusermatchings/update";
        }
        atomProbeUserMatching.merge();
        return "redirect:/admin/atomprobeusermatchings/" + encodeUrlPathSegment(atomProbeUserMatching.getId().toString(), request);
    }
    
    @RequestMapping(value = "/{id}", params = "form", method = RequestMethod.GET)
    public String AtomProbeUserMatchingController.updateForm(@PathVariable("id") Long id, Model model) {
        model.addAttribute("atomProbeUserMatching", AtomProbeUserMatching.findAtomProbeUserMatching(id));
        return "admin/atomprobeusermatchings/update";
    }
    
    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    public String AtomProbeUserMatchingController.delete(@PathVariable("id") Long id, @RequestParam(value = "page", required = false) Integer page, @RequestParam(value = "size", required = false) Integer size, Model model) {
        AtomProbeUserMatching.findAtomProbeUserMatching(id).remove();
        model.addAttribute("page", (page == null) ? "1" : page.toString());
        model.addAttribute("size", (size == null) ? "10" : size.toString());
        return "redirect:/admin/atomprobeusermatchings?page=" + ((page == null) ? "1" : page.toString()) + "&size=" + ((size == null) ? "10" : size.toString());
    }
    
    Converter<AtomProbeUserMatching, String> AtomProbeUserMatchingController.getAtomProbeUserMatchingConverter() {
        return new Converter<AtomProbeUserMatching, String>() {
            public String convert(AtomProbeUserMatching atomProbeUserMatching) {
                return new StringBuilder().append(atomProbeUserMatching.getAtomProbeUsername()).append(" ").append(atomProbeUserMatching.getBookingSystemUsername()).toString();
            }
        };
    }
    
    Converter<StockServer, String> AtomProbeUserMatchingController.getStockServerConverter() {
        return new Converter<StockServer, String>() {
            public String convert(StockServer stockServer) {
                return new StringBuilder().append(stockServer.getDescription()).append(" ").append(stockServer.getProtocol()).append(" ").append(stockServer.getServer()).toString();
            }
        };
    }
    
    @PostConstruct
    void AtomProbeUserMatchingController.registerConverters() {
        conversionService.addConverter(getAtomProbeUserMatchingConverter());
        conversionService.addConverter(getStockServerConverter());
    }
    
    private String AtomProbeUserMatchingController.encodeUrlPathSegment(String pathSegment, HttpServletRequest request) {
        String enc = request.getCharacterEncoding();
        if (enc == null) {
            enc = WebUtils.DEFAULT_CHARACTER_ENCODING;
        }
        try {
            pathSegment = UriUtils.encodePathSegment(pathSegment, enc);
        }
        catch (UnsupportedEncodingException uee) {}
        return pathSegment;
    }
    
}

// RENAME topologyview.js?

function applyToPhysicalSwitchesMappedFrom(switchMapping, virtualSwitchId, cb) {
    var dpid = virtualSwitchId.replace('virtual_switch-', '').replace(/_/g, ':');
    // DEBUG ASSERT?
    if (switchMapping[dpid] && switchMapping[dpid].switches) {
        var physicalSwitches = switchMapping[dpid].switches;
        physicalSwitches.forEach(function (dpid) {
            id = '#physical_switch-' + dpid.replace(/:/g, '_');
            cb(d3.select(id));
        });
    }
}

function applyToInternalLinksOfPhysicalSwitchesMappedFrom(switchMapping, virtualSwitchId, cb) {
    var dpid = virtualSwitchId.replace('virtual_switch-', '').replace(/_/g, ':');
    // DEBUG ASSERT?
    if (switchMapping[dpid] && switchMapping[dpid].links) {
        var internalLinks = switchMapping[dpid].links;
        internalLinks.forEach(function(link) {
            var linkId = '#physical_link-' + link;
            cb(d3.select(linkId));
        });
    }
}

function applyToPhysicalLinksMappedFrom(linkMapping, virtualLinkId, cb) {
    var physicalLinkSets = linkMapping[virtualLinkId];
    // DEBUG ASSERT?
    if (physicalLinkSets) {
        physicalLinkSets.forEach(function (linkSet) {
            linkSet.forEach(function (linkId) {
                id = '#physical_link-' + linkId;
                cb(d3.select(id));
            });
        });
    }
}

function applyToPhysicalSwitchesReferencedByPhysicalLinksMappedFrom(linkMapping, virtualLinkId, cb, filterStartEnd) {
    var physicalLinkMap = {};
    model.physicalTopology.links.forEach(function (link) {
        physicalLinkMap[link.linkId] = {
            src: link.src.dpid,
            dst: link.dst.dpid
        };
    });

    var physicalLinkSets = linkMapping[virtualLinkId];

    // DEBUG ASSERT?
    if (physicalLinkSets) {
        physicalLinkSets.forEach(function (physicalLinks) {
            var i;
            for (i=0; i < physicalLinks.length; i += 1) {
                physLinkId = physicalLinks[i];
                var link = physicalLinkMap[physLinkId];

                if (link) {
                    if (!filterStartEnd || i != 0) {
                        var srcId = '#physical_switch-' + link.src.replace(/:/g, '_');
                        cb(d3.select(srcId));
                    }

                    if (!filterStartEnd || i != physicalLinks.length-1) {
                        var dstId = '#physical_switch-' + link.dst.replace(/:/g, '_');
                        cb(d3.select(dstId));
                    }
                }
            };
        });
    }
}

function selectedVirtualNetwork() {
    return d3.select('#virtualNetworkSelector .virtualNetwork.selected').attr('tenantId');
}

function linking() {
    return d3.select(document.body).classed('linking');
}

function deriveClassesFromIds(which, svg) {
    svg.selectAll('.node').each(function() {
        if (this.getAttribute('id').match('switch')) {
            d3.select(this).classed('switch', true);
        }
        if (this.getAttribute('id').match('host')) {
            d3.select(this).classed('host', true);
        }
    });

    svg.selectAll('.edge').each(function() {
        if (this.getAttribute('id').match('link')) {
            d3.select(this).classed('link', true);
        }
        if (!this.getAttribute('id').match('host')) {
            d3.select(this).classed('real', true);
        }
    });
}

function addMouseareaToPaths(svg) {
    svg.selectAll('.edge').each(function() {
        var edge = d3.select(this);
        var d = edge.select('path').attr('d');
        edge.select('path').classed('displayarea', true);
        edge.append('path').attr('d', d).classed('mousearea', true);
    });
}

function moveNodesToFront(svg) {
    svg.selectAll('.node').each(function() {
        d3.select(this).moveToFront();
    });
}

function moveUsedHostsToFront(container) {
    container.selectAll('.host.used').each(function() {
        d3.select(this).moveToFront();
    });
}

function removeTopBackground(svg) {
    svg.select('polygon').remove();
}

function drawSVGTopology(which, svg) {

    // should these go up a scope?
    svg.setAttribute('id', which + '-svg');
    svg.setAttribute('preserveAspectRatio', true);

    var parent = document.getElementById(which);
    parent.innerHTML = '';
    parent.appendChild(svg);

    // TODO: consistent use of node vs domelement in function calls
    svg = d3.select(svg);

    parent.style['width'] = svg.attr('width').replace('pt', 'px');
    parent.style['height'] = svg.attr('height').replace('pt', 'px');



    // parse the ids generated by the svg server and apply classes
    deriveClassesFromIds(which, svg);

    // create a wider area so that mouseover events are more tolerant
    addMouseareaToPaths(svg);

    // remove the background that graphviz is inserting
    removeTopBackground(svg);
    return svg;
}

